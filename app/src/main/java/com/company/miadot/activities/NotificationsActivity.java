package com.company.miadot.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.adapters.NotificationAdapter;
import com.company.miadot.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList, notif -> {
            if ("like".equals(notif.type) || "comment".equals(notif.type) || "share".equals(notif.type)) {
                // Abrir post relacionado
                // Exemplo: abrir Activity de detalhes do post
                // Intent intent = new Intent(this, PostDetailActivity.class);
                // intent.putExtra("postId", notif.postId);
                // startActivity(intent);
            } else if ("follow".equals(notif.type) || "mention".equals(notif.type)) {
                // Abrir perfil do usuário remetente
                // Intent intent = new Intent(this, ProfileActivity.class);
                // intent.putExtra("userId", notif.senderId);
                // startActivity(intent);
            } else if ("adoption_interest".equals(notif.type)) {
                // Exemplo: abrir tela para aceitar/recusar interesse
                // Intent intent = new Intent(this, AdoptionInterestActivity.class);
                // intent.putExtra("postId", notif.postId);
                // intent.putExtra("interestedUserId", notif.senderId);
                // startActivity(intent);
            }
            // Adapte para outros tipos conforme necessário
        });
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listenForNotifications();
    }

    private void listenForNotifications() {
        db.collection("notificacoes").document(currentUserId).collection("itens")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                Map<String, List<Notification>> grouped = new HashMap<>();
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Notification notif = dc.getDocument().toObject(Notification.class);
                        // Agrupa por tipo+postId (ou outro contexto)
                        String key = notif.type + "_" + (notif.postId != null ? notif.postId : "") + (notif.commentId != null ? ("_"+notif.commentId) : "");
                        if (!grouped.containsKey(key)) grouped.put(key, new ArrayList<>());
                        grouped.get(key).add(notif);
                    }
                }
                notificationList.clear();
                for (List<Notification> group : grouped.values()) {
                    if (group.size() == 1) {
                        notificationList.add(group.get(0));
                    } else {
                        // Cria notificação agrupada
                        Notification first = group.get(0);
                        Notification groupedNotif = new Notification();
                        groupedNotif.type = first.type;
                        groupedNotif.postId = first.postId;
                        groupedNotif.commentId = first.commentId;
                        groupedNotif.read = false;
                        groupedNotif.timestamp = first.timestamp;
                        // Exemplo de texto agrupado
                        StringBuilder sb = new StringBuilder();
                        sb.append(group.get(0).senderName);
                        if (group.size() > 1) sb.append(", ").append(group.get(1).senderName);
                        if (group.size() > 2) sb.append(" e +").append(group.size()-2);
                        if ("like".equals(first.type)) {
                            sb.append(" curtiram seu post");
                        } else if ("comment".equals(first.type)) {
                            sb.append(" comentaram no seu post");
                        } // Adapte para outros tipos
                        groupedNotif.text = sb.toString();
                        notificationList.add(groupedNotif);
                    }
                }
                adapter.notifyDataSetChanged();
            });
    }
}
