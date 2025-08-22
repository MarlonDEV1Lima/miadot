package com.company.miadot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.model.PrivateChat;
import com.company.miadot.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrivateChatAdapter extends RecyclerView.Adapter<PrivateChatAdapter.ChatViewHolder> {
    private List<PrivateChat> chatList;
    private String currentUserId;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(PrivateChat chat);
    }

    public PrivateChatAdapter(List<PrivateChat> chatList, String currentUserId, OnChatClickListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_private, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        PrivateChat chat = chatList.get(position);
        String otherUserId = chat.getOtherUserId(currentUserId);

        // Carregar informações do outro usuário
        // Mostrar placeholder enquanto carrega
        holder.textNome.setText("Carregando...");

        // Tentar primeiro com "users" e depois com "usuarios" se não encontrar
        FirebaseDatabase.getInstance().getReference("users").child(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User usuario = snapshot.getValue(User.class);
                            if (usuario != null) {
                                String displayName = usuario.getNickname() != null ?
                                    usuario.getNickname() : usuario.getFullName();
                                holder.textNome.setText(displayName != null ? displayName : "Usuário");

                                if (usuario.getPhotoUrl() != null && !usuario.getPhotoUrl().isEmpty()) {
                                    Glide.with(holder.itemView.getContext())
                                            .load(usuario.getPhotoUrl())
                                            .circleCrop()
                                            .placeholder(R.drawable.ic_person)
                                            .into(holder.imageProfile);
                                }
                                return;
                            }
                        }

                        // Se não encontrou em "users", tentar em "usuarios"
                        FirebaseDatabase.getInstance().getReference("usuarios").child(otherUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            User usuario = snapshot.getValue(User.class);
                                            if (usuario != null) {
                                                String displayName = usuario.getNickname() != null ?
                                                    usuario.getNickname() : usuario.getFullName();
                                                holder.textNome.setText(displayName != null ? displayName : "Usuário");

                                                if (usuario.getPhotoUrl() != null && !usuario.getPhotoUrl().isEmpty()) {
                                                    Glide.with(holder.itemView.getContext())
                                                            .load(usuario.getPhotoUrl())
                                                            .circleCrop()
                                                            .placeholder(R.drawable.ic_person)
                                                            .into(holder.imageProfile);
                                                }
                                            } else {
                                                holder.textNome.setText("Usuário");
                                            }
                                        } else {
                                            // Debug: mostrar o ID se não encontrar o usuário
                                            holder.textNome.setText("ID: " + otherUserId.substring(0, Math.min(8, otherUserId.length())));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        holder.textNome.setText("Erro");
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.textNome.setText("Erro");
                    }
                });

        // Configurar última mensagem
        if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
            holder.textLastMessage.setText(chat.getLastMessage());
        } else {
            holder.textLastMessage.setText("Toque para iniciar conversa");
        }

        // Configurar tempo da última mensagem
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.textTime.setText(sdf.format(new Date(chat.getLastMessageTime())));

        // Mostrar indicador de mensagens não lidas
        if (chat.hasUnreadFor(currentUserId)) {
            holder.viewUnreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.viewUnreadIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textNome, textLastMessage, textTime;
        View viewUnreadIndicator;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textNome = itemView.findViewById(R.id.textNome);
            textLastMessage = itemView.findViewById(R.id.textLastMessage);
            textTime = itemView.findViewById(R.id.textTime);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }
    }
}
