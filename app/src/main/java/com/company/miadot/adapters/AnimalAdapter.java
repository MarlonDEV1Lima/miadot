package com.company.miadot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.activities.ComentariosBottomSheet;
import com.company.miadot.activities.ProfileActivity;
import com.company.miadot.model.Animal;
import com.company.miadot.model.Comentarios;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private final Context context;
    private final List<Animal> animalList;
    private final String currentUserId; // Adicionado para o ID do usuário logado
    private static final String TAG = "AnimalAdapter";

    public AnimalAdapter(Context context, List<Animal> animalList) {
        this.context = context;
        this.animalList = animalList;
        // Obtenha o ID do usuário logado no construtor
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.currentUserId = (currentUser != null) ? currentUser.getUid() : null;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_animal, parent, false);
        return new AnimalViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animalList.get(position);
        holder.comentariosVisiveis = 3;
        holder.textLikes.setText(String.valueOf(animal.getLikes()));

        String donoId = animal.getDonoId(); // ID do dono da postagem
        if (donoId != null && !donoId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(donoId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    holder.textViewUserName.setText(nickname != null ? nickname : "Usuário");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(context)
                                .load(photoUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .circleCrop()
                                .into(holder.imageViewUserAvatar);
                    } else {
                        holder.imageViewUserAvatar.setImageResource(R.drawable.default_profile);
                    }

                    // --- Lógica do Botão de Seguir/Seguindo ---
                    if (currentUserId != null && !currentUserId.equals(donoId)) { // Não mostrar botão para o próprio usuário
                        holder.buttonFollow.setVisibility(View.VISIBLE);
                        checkFollowStatus(donoId, holder.buttonFollow); // Verifica o status e atualiza o botão
                    } else {
                        holder.buttonFollow.setVisibility(View.GONE); // Esconde o botão se for o próprio usuário
                    }

                    // Define o clique para abrir o perfil do usuário
                    holder.imageViewUserAvatar.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("userId", donoId); // passa o dono do animal para abrir perfil
                        context.startActivity(intent);
                    });
                    // O nome do usuário também deve levar ao perfil
                    holder.textViewUserName.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("userId", donoId); // passa o dono do animal para abrir perfil
                        context.startActivity(intent);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.textViewUserName.setText("Usuário");
                    holder.imageViewUserAvatar.setImageResource(R.drawable.default_profile);
                    holder.buttonFollow.setVisibility(View.GONE); // Esconde o botão em caso de erro
                }
            });
        }


        holder.buttonComment.setOnClickListener(v -> {
            ComentariosBottomSheet.novaInstancia(animal.getId())
                    .show(((AppCompatActivity) context).getSupportFragmentManager(), "ComentariosBottomSheet");
        });

        Glide.with(context).load(animal.getImageURL()).placeholder(R.drawable.placeholder_image).into(holder.imageAnimal);

        // Lógica de Curtidas (já existente)
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animal.getId())
                .child("curtidas")
                .child(uid);

        DatabaseReference likesCountRef = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animal.getId())
                .child("likes");

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.buttonLike.setImageResource(snapshot.exists() ? R.drawable.like_icon : R.drawable.unlike);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao verificar curtida", error.toException());
            }
        });

        loadComentarios(animal.getId(), holder);

        holder.imageAnimal.setOnClickListener(v -> {
            if (holder.popup != null && holder.popup.isShowing()) {
                holder.popup.dismiss();
                return;
            }

            View popupView = LayoutInflater.from(context).inflate(R.layout.popup_zoom_image, null);
            ImageView imageViewZoom = popupView.findViewById(R.id.imageViewZoom);
            TextView textViewAnimalName = popupView.findViewById(R.id.textViewAnimalName);
            TextView textViewLikesPopup = popupView.findViewById(R.id.textViewLikesPopup);
            TextView textViewCommentsPopup = popupView.findViewById(R.id.textViewCommentsPopup);

            Glide.with(context).load(animal.getImageURL()).into(imageViewZoom);
            textViewAnimalName.setText(animal.getNome());
            textViewLikesPopup.setText(animal.getLikes() + " curtidas");
            textViewCommentsPopup.setText("Carregando comentários...");

            holder.popup = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    true);

            popupView.setOnClickListener(view -> holder.popup.dismiss());
            holder.popup.showAtLocation(v, Gravity.CENTER, 0, 0);

            DatabaseReference comentariosRef = FirebaseDatabase.getInstance()
                    .getReference("animais")
                    .child(animal.getId())
                    .child("comentarios");

            comentariosRef.orderByChild("timestamp").limitToLast(3)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StringBuilder sb = new StringBuilder();
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Comentarios c = snap.getValue(Comentarios.class);
                                if (c != null) {
                                    sb.append(c.getNome()).append(": ").append(c.getTexto()).append("\n");
                                }
                            }
                            textViewCommentsPopup.setText(sb.length() == 0 ? "Seja o primeiro a comentar!" : sb.toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            textViewCommentsPopup.setText("Erro ao carregar comentários.");
                        }
                    });
        });

        holder.buttonEnviarComentario.setOnClickListener(v -> {
            String texto = holder.editComentario.getText().toString().trim();
            if (TextUtils.isEmpty(texto)) {
                Toast.makeText(context, "Digite um comentário", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fotoUrl = snapshot.child("photoUrl").getValue(String.class);
                    String nickname = snapshot.child("nickname").getValue(String.class);

                    Comentarios novoComentario = new Comentarios();
                    novoComentario.setNome(nickname != null ? nickname : "Usuário");
                    novoComentario.setTexto(texto);
                    novoComentario.setTimestamp(System.currentTimeMillis());
                    novoComentario.setFotoUrl(fotoUrl != null ? fotoUrl : "");

                    String comentarioPaiId = (String) holder.editComentario.getTag();

                    if (comentarioPaiId != null) {
                        String respostaId = FirebaseDatabase.getInstance()
                                .getReference("respostas")
                                .child(comentarioPaiId)
                                .push()
                                .getKey();

                        if (respostaId != null) {
                            novoComentario.setId(respostaId);
                            FirebaseDatabase.getInstance()
                                    .getReference("respostas")
                                    .child(comentarioPaiId)
                                    .child(respostaId)
                                    .setValue(novoComentario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Resposta enviada!", Toast.LENGTH_SHORT).show();
                                        holder.editComentario.setText("");
                                        holder.editComentario.setHint("Adicione um comentário...");
                                        holder.editComentario.setTag(null);
                                        loadComentarios(animal.getId(), holder);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Erro ao enviar resposta", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        String comentarioId = FirebaseDatabase.getInstance()
                                .getReference("animais")
                                .child(animal.getId())
                                .child("comentarios")
                                .push()
                                .getKey();

                        if (comentarioId != null) {
                            novoComentario.setId(comentarioId);
                            FirebaseDatabase.getInstance()
                                    .getReference("animais")
                                    .child(animal.getId())
                                    .child("comentarios")
                                    .child(comentarioId)
                                    .setValue(novoComentario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Comentário enviado!", Toast.LENGTH_SHORT).show();
                                        holder.editComentario.setText("");
                                        loadComentarios(animal.getId(), holder);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Erro ao enviar comentário", Toast.LENGTH_SHORT).show());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Erro ao buscar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Configura o Listener para o botão de seguir (DEPOIS de ter o donoId)
        holder.buttonFollow.setOnClickListener(v -> toggleFollow(donoId, holder.buttonFollow));

        // Botão "Tenho interesse" abre o chat com mensagem automática
        holder.buttonTenhoInteresse.setOnClickListener(v -> {
            if (donoId == null || donoId.isEmpty()) {
                Toast.makeText(context, "Erro: dono do animal não identificado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUserId == null || currentUserId.equals(donoId)) {
                Toast.makeText(context, "Você não pode conversar com você mesmo", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, com.company.miadot.activities.ChatActivity.class);
            intent.putExtra("otherUserId", donoId); // Corrigido: usar otherUserId em vez de donoId
            intent.putExtra("animalId", animal.getId());
            intent.putExtra("animalNome", animal.getNome());
            context.startActivity(intent);
        });

        // Lógica de like/unlike
        holder.buttonLike.setOnClickListener(v -> {
            if (currentUserId == null) return;
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Já curtiu, então remove o like
                        likeRef.removeValue();
                        int newLikes = Math.max(0, animal.getLikes() - 1);
                        likesCountRef.setValue(newLikes);
                        animal.setLikes(newLikes);
                        holder.textLikes.setText(String.valueOf(newLikes));
                        holder.buttonLike.setImageResource(R.drawable.unlike);
                    } else {
                        // Ainda não curtiu, então adiciona o like
                        likeRef.setValue(true);
                        int newLikes = animal.getLikes() + 1;
                        likesCountRef.setValue(newLikes);
                        animal.setLikes(newLikes);
                        holder.textLikes.setText(String.valueOf(newLikes));
                        holder.buttonLike.setImageResource(R.drawable.like_icon);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }

    private void loadComentarios(String animalId, AnimalViewHolder holder) {
        DatabaseReference refComentarios = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animalId)
                .child("comentarios");

        refComentarios.orderByChild("timestamp").limitToLast(holder.comentariosVisiveis)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Comentarios> lista = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Comentarios c = snap.getValue(Comentarios.class);
                            if (c != null) lista.add(c);
                        }

                        ComentarioAdapter adapter = new ComentarioAdapter(context, lista, false);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                        layoutManager.setReverseLayout(true);
                        layoutManager.setStackFromEnd(true);
                        holder.recyclerComentarios.setLayoutManager(layoutManager);

                        holder.recyclerComentarios.setAdapter(adapter);

                        adapter.setOnResponderClickListener(comentarioSelecionado -> {
                            holder.editComentario.requestFocus();
                            holder.editComentario.setHint("Respondendo a " + comentarioSelecionado.getNome());
                            holder.editComentario.setTag(comentarioSelecionado.getId());
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Erro ao carregar comentários", error.toException());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return animalList.size();
    }

    /**
     * Verifica o status de seguir para o usuário dono da postagem e atualiza o botão.
     * @param donoId O ID do usuário dono da postagem.
     * @param followButton O botão de Seguir/Seguindo.
     */
    private void checkFollowStatus(String donoId, Button followButton) {
        if (currentUserId == null || donoId == null) {
            followButton.setVisibility(View.GONE);
            return;
        }

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("follows")
                .child(currentUserId)
                .child("following")
                .child(donoId);

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Já está seguindo
                    followButton.setText("Seguindo");
                    followButton.setBackgroundResource(R.drawable.rounded_button_gray); // Crie este drawable
                    followButton.setTextColor(context.getResources().getColor(R.color.text_primary));
                } else {
                    // Não está seguindo
                    followButton.setText("Seguir");
                    followButton.setBackgroundResource(R.drawable.rounded_button_blue);
                    followButton.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao verificar status de seguir: " + error.getMessage());
                followButton.setVisibility(View.GONE); // Esconde o botão em caso de erro
            }
        });
    }

    /**
     * Alterna o status de seguir/deixar de seguir para um usuário.
     * @param targetUserId O ID do usuário a ser seguido/deixado de seguir.
     * @param followButton O botão de Seguir/Seguindo.
     */
    private void toggleFollow(String targetUserId, Button followButton) {
        if (currentUserId == null || targetUserId == null) {
            Toast.makeText(context, "Erro: Usuário não logado ou ID de destino inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referências para o Realtime Database
        DatabaseReference currentUserFollowingRef = FirebaseDatabase.getInstance().getReference("follows")
                .child(currentUserId)
                .child("following")
                .child(targetUserId);

        DatabaseReference targetUserFollowersRef = FirebaseDatabase.getInstance().getReference("follows")
                .child(targetUserId)
                .child("followers")
                .child(currentUserId);

        // Verifica o status atual para alternar
        currentUserFollowingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Se já segue, então "deixar de seguir"
                    currentUserFollowingRef.removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    targetUserFollowersRef.removeValue(); // Remove o seguidor da lista do alvo
                                    followButton.setText("Seguir");
                                    followButton.setBackgroundResource(R.drawable.rounded_button_blue);
                                    followButton.setTextColor(context.getResources().getColor(android.R.color.white));
                                    Toast.makeText(context, "Você deixou de seguir " + targetUserId.substring(0, 8) + "...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Erro ao deixar de seguir.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Erro ao remover following: " + task.getException().getMessage());
                                }
                            });
                } else {
                    // Se não segue, então "seguir"
                    currentUserFollowingRef.setValue(true)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    targetUserFollowersRef.setValue(true); // Adiciona o seguidor na lista do alvo
                                    followButton.setText("Seguindo");
                                    followButton.setBackgroundResource(R.drawable.rounded_button_gray);
                                    followButton.setTextColor(context.getResources().getColor(R.color.text_primary));
                                    Toast.makeText(context, "Você está seguindo " + targetUserId.substring(0, 8) + "!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Erro ao seguir.", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Erro ao adicionar following: " + task.getException().getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao alternar seguir/deixar de seguir: " + error.getMessage());
                Toast.makeText(context, "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    class AnimalViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAnimal, imageViewUserAvatar;
        TextView textLikes, textViewUserName;
        ImageView buttonLike, buttonComment; // Corrigido: ImageView em vez de ImageButton
        LinearLayout layoutComentarios;
        EditText editComentario;
        Button buttonEnviarComentario;
        PopupWindow popup;
        RecyclerView recyclerComentarios;
        int comentariosVisiveis = 3;
        Button buttonFollow; // Referência para o novo botão de seguir
        public Button buttonTenhoInteresse;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAnimal = itemView.findViewById(R.id.imageViewAnimal);
            textLikes = itemView.findViewById(R.id.textViewLikes);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonComment = itemView.findViewById(R.id.buttonComment);
            editComentario = itemView.findViewById(R.id.editComentario);
            buttonEnviarComentario = itemView.findViewById(R.id.buttonEnviarComentario);
            layoutComentarios = itemView.findViewById(R.id.layoutComentarios);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            recyclerComentarios = itemView.findViewById(R.id.recyclerViewComentarios);
            buttonFollow = itemView.findViewById(R.id.buttonFollow); // Inicializa o novo botão
            buttonTenhoInteresse = itemView.findViewById(R.id.buttonTenhoInteresse);
        }
    }
}
