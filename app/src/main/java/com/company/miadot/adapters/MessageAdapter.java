package com.company.miadot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.model.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.textMessageSender.setText(msg.senderId.equals(currentUserId) ? "Você" : "Outro");
        holder.textMessageContent.setText(msg.text);
        holder.textMessageTime.setText(""); // Formatar timestamp
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMessageSender, textMessageContent, textMessageTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessageSender = itemView.findViewById(R.id.textMessageSender);
            textMessageContent = itemView.findViewById(R.id.textMessageContent);
            textMessageTime = itemView.findViewById(R.id.textMessageTime);
        }
    }
}

