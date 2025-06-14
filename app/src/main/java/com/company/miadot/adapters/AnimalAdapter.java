package com.company.miadot.adapters;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import androidx.annotation.Nullable;

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

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animalList.get(position);

        holder.textNome.setText(animal.getNome());
        holder.textLikes.setText(String.valueOf(animal.getLikes()));
        holder.textInteressados.setText(String.valueOf(animal.getInteressados()));

        // Limpa qualquer carregamento anterior da imagem (importante para RecyclerView)
        Glide.with(context).clear(holder.imageAnimal);

        // Carrega a imagem com Glide
        Glide.with(context)
                .load(animal.getImageURL())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageAnimal);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Setar estado padrão do botão de curtir (não curtido)
        holder.buttonLike.setImageResource(R.drawable.unlike);
        holder.buttonLike.setEnabled(true);

        // Consultar Firebase para saber se o usuário já curtiu esse animal
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

        // Clique no botão curtir
        holder.buttonLike.setOnClickListener(v -> {
            DatabaseReference animalRef = FirebaseDatabase.getInstance()
                    .getReference("animais")
                    .child(animal.getId());

            animalRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Animal animalData = currentData.getValue(Animal.class);
                    if (animalData == null) return Transaction.success(currentData);

                    if (animalData.getCurtidas() == null) {
                        animalData.setCurtidas(new java.util.HashMap<>());
                    }

                    boolean jaCurtiu = animalData.getCurtidas().containsKey(uid);
                    int currentLikes = animalData.getLikes() != null ? animalData.getLikes() : 0;

                    if (jaCurtiu) {
                        animalData.getCurtidas().remove(uid);
                        animalData.setLikes(Math.max(0, currentLikes - 1));
                    } else {
                        animalData.getCurtidas().put(uid, true);
                        animalData.setLikes(currentLikes + 1);
                    }

                    currentData.setValue(animalData);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (committed && currentData != null) {
                        Animal updatedAnimal = currentData.getValue(Animal.class);
                        if (updatedAnimal != null) {
                            // Atualiza texto e estado do botão
                            holder.textLikes.setText(String.valueOf(updatedAnimal.getLikes()));
                            animal.setLikes(updatedAnimal.getLikes());
                            boolean jaCurtiu = updatedAnimal.getCurtidas() != null &&
                                    updatedAnimal.getCurtidas().containsKey(uid);
                            holder.buttonLike.setImageResource(jaCurtiu ? R.drawable.like_icon : R.drawable.unlike);
                            Toast.makeText(context,
                                    jaCurtiu ? "Você curtiu este animal!" : "Você removeu a curtida.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (error != null) {
                        Log.e("Firebase", "Erro na transação de curtida", error.toException());
                    }
                }
            });
        });

        // Carrega os comentários
        loadComentarios(animal.getId(), holder);

        // Botão para mostrar/esconder campo de comentário
        holder.buttonComment.setOnClickListener(v -> {
            if (holder.layoutComentarios.getVisibility() == View.GONE) {
                holder.layoutComentarios.setVisibility(View.VISIBLE);
            } else {
                holder.layoutComentarios.setVisibility(View.GONE);
            }
        });

        // Enviar comentário
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
            novoComentario.setNome(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
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
                        loadComentarios(animal.getId(), holder); // Atualiza a lista de comentários
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
        ImageView imageAnimal;
        TextView textNome, textLikes, textInteressados, textComentarios;
        ImageButton buttonLike, buttonComment;
        LinearLayout layoutComentarios;
        EditText editComentario;
        Button buttonEnviarComentario;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAnimal = itemView.findViewById(R.id.imageViewAnimal);
            textNome = itemView.findViewById(R.id.textViewNome);
            textLikes = itemView.findViewById(R.id.textViewLikes);
            textInteressados = itemView.findViewById(R.id.textViewInteressados);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonComment = itemView.findViewById(R.id.buttonComment);
            layoutComentarios = itemView.findViewById(R.id.layoutComentarios);
            textComentarios = itemView.findViewById(R.id.textComentarios);
            editComentario = itemView.findViewById(R.id.editComentario);
            buttonEnviarComentario = itemView.findViewById(R.id.buttonEnviarComentario);
        }
    }
}
