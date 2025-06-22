package com.company.miadot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.company.miadot.model.Animal;
import com.company.miadot.model.Comentarios;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private final Context context;
    private final List<Animal> animalList;

    public AnimalAdapter(Context context, List<Animal> animalList) {
        this.context = context;
        this.animalList = animalList;
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

        String donoId = animal.getDonoId();
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.textViewUserName.setText("Usuário");
                    holder.imageViewUserAvatar.setImageResource(R.drawable.default_profile);
                }
            });
        }

        holder.buttonComment.setOnClickListener(v -> {
            ComentariosBottomSheet.novaInstancia(animal.getId())
                    .show(((AppCompatActivity) context).getSupportFragmentManager(), "ComentariosBottomSheet");
        });

        Glide.with(context).load(animal.getImageURL()).placeholder(R.drawable.placeholder_image).into(holder.imageAnimal);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animal.getId())
                .child("curtidas")
                .child(uid);

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

    class AnimalViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAnimal, imageViewUserAvatar;
        TextView textLikes, textViewUserName;
        ImageButton buttonLike, buttonComment;
        LinearLayout layoutComentarios;
        EditText editComentario;
        Button buttonEnviarComentario;
        PopupWindow popup;
        RecyclerView recyclerComentarios;
        int comentariosVisiveis = 3;

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
        }
    }
}
