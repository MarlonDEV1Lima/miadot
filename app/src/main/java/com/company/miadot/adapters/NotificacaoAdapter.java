package com.company.miadot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.model.Notificacao;
import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder> {
    private List<Notificacao> notificacoes;
    private OnNotificacaoClickListener listener;

    public interface OnNotificacaoClickListener {
        void onNotificacaoClick(Notificacao notificacao);
    }

    public NotificacaoAdapter(List<Notificacao> notificacoes, OnNotificacaoClickListener listener) {
        this.notificacoes = notificacoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacao, parent, false);
        return new NotificacaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacaoViewHolder holder, int position) {
        Notificacao notificacao = notificacoes.get(position);
        holder.textMensagem.setText(notificacao.getMensagem());
        holder.textData.setText(android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", notificacao.getTimestamp()));
        holder.itemView.setAlpha(notificacao.isLida() ? 0.5f : 1f);
        holder.itemView.setOnClickListener(v -> listener.onNotificacaoClick(notificacao));
        // Imagem e outros campos podem ser configurados aqui
    }

    @Override
    public int getItemCount() {
        return notificacoes.size();
    }

    public static class NotificacaoViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagem, textData;
        ImageView imageIcone;
        public NotificacaoViewHolder(@NonNull View itemView) {
            super(itemView);
            textMensagem = itemView.findViewById(R.id.textMensagem);
            textData = itemView.findViewById(R.id.textData);
            imageIcone = itemView.findViewById(R.id.imageIcone);
        }
    }
}

