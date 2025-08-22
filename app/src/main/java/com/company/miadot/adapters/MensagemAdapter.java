package com.company.miadot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.model.Mensagem;
import com.company.miadot.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.MensagemViewHolder> {
    private List<Mensagem> mensagens;
    private String currentUserId;

    public MensagemAdapter(List<Mensagem> mensagens, String currentUserId) {
        this.mensagens = mensagens;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MensagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem, parent, false);
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        Mensagem mensagem = mensagens.get(position);

        // Formatação da data/hora
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeFormatted = sdf.format(new Date(mensagem.getTimestamp()));

        boolean isMessageFromCurrentUser = mensagem.getRemetenteId().equals(currentUserId);

        if (isMessageFromCurrentUser) {
            // Mensagem enviada pelo usuário atual
            holder.layoutMensagemRecebida.setVisibility(View.GONE);
            holder.layoutMensagemEnviada.setVisibility(View.VISIBLE);

            holder.textMensagemEnviada.setText(mensagem.getTexto());
            holder.textDataEnviada.setText(timeFormatted);

            // Configurar status da mensagem (lida/não lida)
            if (mensagem.isVisualizada()) {
                holder.imageStatus.setVisibility(View.VISIBLE);
                // Aqui você pode adicionar lógica para diferentes tipos de status
            } else {
                holder.imageStatus.setVisibility(View.VISIBLE);
            }

        } else {
            // Mensagem recebida de outro usuário
            holder.layoutMensagemEnviada.setVisibility(View.GONE);
            holder.layoutMensagemRecebida.setVisibility(View.VISIBLE);

            holder.textMensagemRecebida.setText(mensagem.getTexto());
            holder.textDataRecebida.setText(timeFormatted);

            // Garantir que o TextView do nome está visível
            holder.textNomeRemetente.setVisibility(View.VISIBLE);
            // Mostrar um placeholder enquanto carrega
            holder.textNomeRemetente.setText("Carregando...");

            // Carregar nome do remetente
            loadSenderName(mensagem.getRemetenteId(), holder.textNomeRemetente);
        }
    }

    private void loadSenderName(String senderId, TextView textViewName) {
        // Tentar primeiro com "users" e depois com "usuarios" se não encontrar
        FirebaseDatabase.getInstance().getReference("users").child(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                String displayName = user.getNickname() != null ?
                                    user.getNickname() : user.getFullName();
                                textViewName.setText(displayName != null ? displayName : "Usuário");
                                return;
                            }
                        }

                        // Se não encontrou em "users", tentar em "usuarios"
                        FirebaseDatabase.getInstance().getReference("usuarios").child(senderId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            User user = snapshot.getValue(User.class);
                                            if (user != null) {
                                                String displayName = user.getNickname() != null ?
                                                    user.getNickname() : user.getFullName();
                                                textViewName.setText(displayName != null ? displayName : "Usuário");
                                            } else {
                                                textViewName.setText("Usuário");
                                            }
                                        } else {
                                            // Debug: mostrar o ID se não encontrar o usuário
                                            textViewName.setText("ID: " + senderId.substring(0, Math.min(8, senderId.length())));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        textViewName.setText("Erro");
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textViewName.setText("Erro");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    public static class MensagemViewHolder extends RecyclerView.ViewHolder {
        // Views para mensagem recebida
        LinearLayout layoutMensagemRecebida;
        TextView textNomeRemetente;
        TextView textMensagemRecebida;
        TextView textDataRecebida;

        // Views para mensagem enviada
        LinearLayout layoutMensagemEnviada;
        TextView textMensagemEnviada;
        TextView textDataEnviada;
        ImageView imageStatus;

        public MensagemViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar views para mensagem recebida
            layoutMensagemRecebida = itemView.findViewById(R.id.layoutMensagemRecebida);
            textNomeRemetente = itemView.findViewById(R.id.textNomeRemetente);
            textMensagemRecebida = itemView.findViewById(R.id.textMensagemRecebida);
            textDataRecebida = itemView.findViewById(R.id.textDataRecebida);

            // Inicializar views para mensagem enviada
            layoutMensagemEnviada = itemView.findViewById(R.id.layoutMensagemEnviada);
            textMensagemEnviada = itemView.findViewById(R.id.textMensagemEnviada);
            textDataEnviada = itemView.findViewById(R.id.textDataEnviada);
            imageStatus = itemView.findViewById(R.id.imageStatus);
        }
    }
}
