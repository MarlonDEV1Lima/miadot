package com.company.miadot.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.adapters.ConversationAdapter;
import com.company.miadot.model.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

// Esta activity será removida - usar MessagesActivity em vez disso
@Deprecated
public class InboxActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        recyclerView = findViewById(R.id.recyclerViewConversations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(conversationList, conv -> {
            // Abrir chat ao clicar na conversa
            Intent intent = new Intent(this, ChatActivity.class);
            // Descobrir o outro participante
            String otherUserId = null;
            // Lógica para chat privado: identificar o outro usuário da conversa
            if (conv.user1Id != null && conv.user2Id != null && currentUserId != null) {
                if (currentUserId.equals(conv.user1Id)) {
                    otherUserId = conv.user2Id;
                } else if (currentUserId.equals(conv.user2Id)) {
                    otherUserId = conv.user1Id;
                }
            }
            if (otherUserId != null && !otherUserId.isEmpty()) {
                intent.putExtra("otherUserId", otherUserId); // Corrigido para usar otherUserId
                startActivity(intent);
            } else {
                android.util.Log.e("InboxActivity", "Falha ao abrir chat: user1Id=" + conv.user1Id + ", user2Id=" + conv.user2Id + ", currentUserId=" + currentUserId);
                android.widget.Toast.makeText(this, "Erro: não foi possível identificar o destinatário da conversa", android.widget.Toast.LENGTH_LONG).show();
            }
        });
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listenForConversations();
    }

    private void listenForConversations() {
        db.collection("conversas")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                conversationList.clear();
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                        Conversation conv = dc.getDocument().toObject(Conversation.class);
                        conv.id = dc.getDocument().getId();
                        // Buscar mensagens não lidas para esta conversa
                        db.collection("conversas").document(conv.id).collection("mensagens")
                            .whereArrayContains("readBy", currentUserId)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                int unread = 0;
                                for (var doc : querySnapshot.getDocuments()) {
                                    List<String> readBy = (List<String>) doc.get("readBy");
                                    if (readBy == null || !readBy.contains(currentUserId)) {
                                        unread++;
                                    }
                                }
                                conv.unreadCount = unread;
                                // Atualiza ou adiciona conversa na lista
                                boolean found = false;
                                for (int i = 0; i < conversationList.size(); i++) {
                                    if (conversationList.get(i).id.equals(conv.id)) {
                                        conversationList.set(i, conv);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) conversationList.add(conv);
                                adapter.notifyDataSetChanged();
                            });
                    }
                }
            });
    }
}
