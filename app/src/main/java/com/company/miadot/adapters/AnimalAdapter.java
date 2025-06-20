package com.company.miadot.adapters;

import com.google.firebase.auth.FirebaseAuth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.model.Animal;
import com.company.miadot.model.Comentarios;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private Context context;
    private List<Animal> animalList;
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;


    public AnimalAdapter(Context context, List<Animal> animalList) {
        this.context = context;
        this.animalList = animalList;
    }

    @Override
    public int getItemViewType(int position) {
        return animalList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
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

        holder.textNome.setText(animal.getNome());
        holder.textLikes.setText(String.valueOf(animal.getLikes()));

        // Aqui busca o nickname do dono do animal (usuário) no Firebase pelo userId (donoId)
        String donoId = animal.getDonoId(); // Ajuste se seu metodo for getUserId()


        if (donoId != null && !donoId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(donoId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    // Define o nome do usuário
                    if (nickname != null && !nickname.isEmpty()) {
                        holder.textViewUserName.setText(nickname);
                    } else {
                        holder.textViewUserName.setText("Usuário");
                    }

                    // Define a imagem de perfil
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
        } else {
            holder.textViewUserName.setText("Usuário");
            holder.imageViewUserAvatar.setImageResource(R.drawable.default_profile);
        }

        Glide.with(context).clear(holder.imageAnimal);
        Glide.with(context)
                .load(animal.getImageURL())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageAnimal);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        holder.buttonLike.setImageResource(R.drawable.unlike);
        holder.buttonLike.setEnabled(true);

        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animal.getId())
                .child("curtidas")
                .child(uid);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.buttonLike.setImageResource(R.drawable.like_icon);
                } else {
                    holder.buttonLike.setImageResource(R.drawable.unlike);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao verificar curtida", error.toException());
            }
        });

        // Clique para abrir popup com zoom da imagem e comentários
        holder.imageAnimal.setOnClickListener(v -> {
            if (holder.popup != null && holder.popup.isShowing()) {
                holder.popup.dismiss();
                holder.popup = null;
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

            popupView.setOnClickListener(view -> {
                if (holder.popup != null && holder.popup.isShowing()) {
                    holder.popup.dismiss();
                    holder.popup = null;
                }
            });

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
                                    sb.append(c.getNome())
                                            .append(": ")
                                            .append(c.getTexto())
                                            .append("\n");
                                }
                            }
                            if (sb.length() == 0) {
                                textViewCommentsPopup.setText("Seja o primeiro a comentar!");
                            } else {
                                textViewCommentsPopup.setText(sb.toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            textViewCommentsPopup.setText("Erro ao carregar comentários.");
                        }
                    });
        });

        loadComentarios(animal.getId(), holder);

        holder.buttonComment.setOnClickListener(v -> {
            if (holder.layoutComentarios.getVisibility() == View.GONE) {
                holder.layoutComentarios.setVisibility(View.VISIBLE);
            } else {
                holder.layoutComentarios.setVisibility(View.GONE);
            }
        });

        holder.buttonEnviarComentario.setOnClickListener(v -> {
            String texto = holder.editComentario.getText().toString().trim();
            if (TextUtils.isEmpty(texto)) {
                Toast.makeText(context, "Digite um comentário", Toast.LENGTH_SHORT).show();
                return;
            }

            String comentarioId = FirebaseDatabase.getInstance()
                    .getReference("animais")
                    .child(animal.getId())
                    .child("comentarios")
                    .push()
                    .getKey();

            if (comentarioId == null) {
                Toast.makeText(context, "Erro ao gerar ID do comentário", Toast.LENGTH_SHORT).show();
                return;
            }

            Comentarios novoComentario = new Comentarios();
            novoComentario.setId(comentarioId);
            novoComentario.setNome(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
            novoComentario.setTexto(texto);
            novoComentario.setTimestamp(System.currentTimeMillis());

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
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erro ao enviar comentário", Toast.LENGTH_SHORT).show();
                    });
        });

    }

    private void loadComentarios(String animalId, AnimalViewHolder holder) {
        DatabaseReference refComentarios = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animalId)
                .child("comentarios");

        refComentarios.orderByChild("timestamp").limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        StringBuilder comentariosBuilder = new StringBuilder();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Comentarios c = snap.getValue(Comentarios.class);
                            if (c != null) {
                                comentariosBuilder.append(c.getNome())
                                        .append(": ")
                                        .append(c.getTexto())
                                        .append("\n\n");
                            }
                        }
                        if (comentariosBuilder.length() == 0) {
                            comentariosBuilder.append("Seja o primeiro a comentar!");
                        }
                        holder.textComentarios.setText(comentariosBuilder.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Erro ao carregar comentários", error.toException());
                        holder.textComentarios.setText("Erro ao carregar comentários.");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return animalList.size();
    }

    class AnimalViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAnimal, imageViewUserAvatar;
        TextView textNome, textLikes, textComentarios, textViewUserName;
        ImageButton buttonLike, buttonComment;
        LinearLayout layoutComentarios;
        EditText editComentario;
        Button buttonEnviarComentario;
        PopupWindow popup;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAnimal = itemView.findViewById(R.id.imageViewAnimal);
            textNome = itemView.findViewById(R.id.textViewNome);
            textLikes = itemView.findViewById(R.id.textViewLikes);
            textComentarios = itemView.findViewById(R.id.textComentarios);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonComment = itemView.findViewById(R.id.buttonComment);
            editComentario = itemView.findViewById(R.id.editComentario);
            buttonEnviarComentario = itemView.findViewById(R.id.buttonEnviarComentario);
            layoutComentarios = itemView.findViewById(R.id.layoutComentarios);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
        }
    }
}
