package com.company.miadot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.model.Mensagem;
import java.util.List;

public class ChatInboxAdapter extends RecyclerView.Adapter<ChatInboxAdapter.ChatViewHolder> {
    private List<Mensagem> ultimasMensagens;
    private String userId;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Mensagem ultimaMensagem);
    }

    public ChatInboxAdapter(List<Mensagem> ultimasMensagens, String userId, OnChatClickListener listener) {
        this.ultimasMensagens = ultimasMensagens;
        this.userId = userId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_inbox, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Mensagem mensagem = ultimasMensagens.get(position);
        String outroId = mensagem.getRemetenteId().equals(userId) ? mensagem.getDestinatarioId() : mensagem.getRemetenteId();
        holder.textNome.setText("Usuário " + outroId); // Substituir por nome real se disponível
        holder.textMensagem.setText(mensagem.getTexto());
        holder.textData.setText(android.text.format.DateFormat.format("dd/MM HH:mm", mensagem.getTimestamp()));
        holder.imageBadge.setVisibility(!mensagem.isVisualizada() && !mensagem.getRemetenteId().equals(userId) ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onChatClick(mensagem));
    }

    @Override
    public int getItemCount() {
        return ultimasMensagens.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textNome, textMensagem, textData;
        ImageView imageBadge;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textMensagem = itemView.findViewById(R.id.textMensagem);
            textData = itemView.findViewById(R.id.textData);
            imageBadge = itemView.findViewById(R.id.imageBadge);
        }
    }
}

