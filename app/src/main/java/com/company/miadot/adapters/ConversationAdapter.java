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
import com.company.miadot.model.Conversation;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<Conversation> conversations;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, OnConversationClickListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conv = conversations.get(position);
        holder.textConversationName.setText(conv.isGroup ? conv.groupName : "Usuário");
        holder.textLastMessage.setText(conv.lastMessage != null ? conv.lastMessage.text : "");
        Glide.with(holder.itemView.getContext())
                .load(conv.isGroup ? conv.groupPhoto : R.drawable.default_profile)
                .placeholder(R.drawable.default_profile)
                .circleCrop()
                .into(holder.imageConversationPhoto);
        // Exibe badge de não lidas se houver mensagens não lidas
        if (conv.unreadCount > 0) {
            holder.textUnreadCount.setVisibility(View.VISIBLE);
            holder.textUnreadCount.setText(String.valueOf(conv.unreadCount));
        } else {
            holder.textUnreadCount.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onClick(conv));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageConversationPhoto;
        TextView textConversationName, textLastMessage, textUnreadCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageConversationPhoto = itemView.findViewById(R.id.imageConversationPhoto);
            textConversationName = itemView.findViewById(R.id.textConversationName);
            textLastMessage = itemView.findViewById(R.id.textLastMessage);
            textUnreadCount = itemView.findViewById(R.id.textUnreadCount);
        }
    }
}
