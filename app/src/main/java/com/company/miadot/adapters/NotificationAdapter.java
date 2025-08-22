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
import com.company.miadot.model.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifications.get(position);
        holder.textNotificationContent.setText(notif.text);
        Glide.with(holder.itemView.getContext())
                .load(notif.senderPhoto)
                .placeholder(R.drawable.default_profile)
                .circleCrop()
                .into(holder.imageNotificationUser);
        holder.badgeUnread.setVisibility(notif.read ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onClick(notif));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageNotificationUser;
        TextView textNotificationContent;
        View badgeUnread;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageNotificationUser = itemView.findViewById(R.id.imageNotificationUser);
            textNotificationContent = itemView.findViewById(R.id.textNotificationContent);
            badgeUnread = itemView.findViewById(R.id.badgeUnread);
        }
    }
}

