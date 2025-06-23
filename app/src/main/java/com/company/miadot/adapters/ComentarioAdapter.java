package com.company.miadot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.activities.TimeUtils;
import com.company.miadot.model.Comentarios;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private static final int TIPO_COMENTARIO = 0;
    private static final int TIPO_RESPOSTA = 1;

    private final Context context;
    private final List<Comentarios> comentarios;
    private final boolean isResposta;
    private OnResponderClickListener responderClickListener;

    public ComentarioAdapter(Context context, List<Comentarios> comentarios, boolean isResposta) {
        this.context = context;
        this.comentarios = comentarios;
        this.isResposta = isResposta;
    }

    public void setOnResponderClickListener(OnResponderClickListener listener) {
        this.responderClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return isResposta ? TIPO_RESPOSTA : TIPO_COMENTARIO;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TIPO_RESPOSTA) {
            view = LayoutInflater.from(context).inflate(R.layout.item_resposta_comentario, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentarios comentario = comentarios.get(position);

        holder.textNomeUsuario.setText(comentario.getNome());
        holder.textComentario.setText(comentario.getTexto());
        if (holder.textTempo != null) {
            holder.textTempo.setText(TimeUtils.getTimeAgo(comentario.getTimestamp()));
        }


        if (comentario.getFotoUrl() != null && !comentario.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(comentario.getFotoUrl())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(holder.imageAvatar);
        } else {
            holder.imageAvatar.setImageResource(R.drawable.default_profile);
        }

        if (!isResposta) {
            // Comentário principal: mostra "Responder" e carrega respostas
            holder.textResponder.setVisibility(View.VISIBLE);
            holder.textResponder.setOnClickListener(v -> {
                if (responderClickListener != null) {
                    responderClickListener.onResponderClick(comentario);
                }
            });

            loadRespostas(comentario.getId(), holder.recyclerRespostas);
        } else {
            // Resposta: esconde elementos não utilizados
            if (holder.textResponder != null)
                holder.textResponder.setVisibility(View.GONE);
            if (holder.recyclerRespostas != null)
                holder.recyclerRespostas.setVisibility(View.GONE);
        }
    }

    private void loadRespostas(String comentarioId, RecyclerView recyclerRespostas) {
        FirebaseDatabase.getInstance().getReference("respostas")
                .child(comentarioId)
                .orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Comentarios> respostas = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Comentarios resposta = snap.getValue(Comentarios.class);
                            if (resposta != null) respostas.add(resposta);
                        }

                        if (!respostas.isEmpty()) {
                            ComentarioAdapter respostaAdapter = new ComentarioAdapter(context, respostas, true);
                            respostaAdapter.setOnResponderClickListener(responderClickListener);

                            recyclerRespostas.setLayoutManager(new LinearLayoutManager(context));
                            recyclerRespostas.setAdapter(respostaAdapter);
                            recyclerRespostas.setVisibility(View.VISIBLE);
                        } else {
                            recyclerRespostas.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        recyclerRespostas.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    public interface OnResponderClickListener {
        void onResponderClick(Comentarios comentario);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNomeUsuario, textComentario;
        TextView textResponder; // visível só em comentário principal
        ImageView imageAvatar;
        RecyclerView recyclerRespostas; // usado somente se não for resposta
        TextView textTempo;

        public ViewHolder(@NonNull View itemView, int tipo) {
            super(itemView);

            textTempo = itemView.findViewById(R.id.textTempo);

            if (tipo == TIPO_COMENTARIO) {
                textNomeUsuario = itemView.findViewById(R.id.textNomeUsuario);
                textComentario = itemView.findViewById(R.id.textComentario);
                textResponder = itemView.findViewById(R.id.textResponder);
                imageAvatar = itemView.findViewById(R.id.imageViewUserAvatarComentario);
                recyclerRespostas = itemView.findViewById(R.id.recyclerViewRespostas);
            } else {
                textNomeUsuario = itemView.findViewById(R.id.textNomeUsuarioResposta);
                textComentario = itemView.findViewById(R.id.textComentarioResposta);
                imageAvatar = itemView.findViewById(R.id.imageViewAvatarResposta);
                textResponder = null;
                recyclerRespostas = null;
            }
        }
    }
}
