package com.company.miadot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.adapters.PrivateChatAdapter;
import com.company.miadot.model.PrivateChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private RecyclerView recyclerChats;
    private TextView textEmptyState;
    private PrivateChatAdapter adapter;
    private List<PrivateChat> chatList = new ArrayList<>();
    private DatabaseReference chatsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initViews();
        setupRecyclerView();
        loadChats();
    }

    private void initViews() {
        recyclerChats = findViewById(R.id.recyclerChats);
        textEmptyState = findViewById(R.id.textEmptyState);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference("private_chats");
    }

    private void setupRecyclerView() {
        adapter = new PrivateChatAdapter(chatList, currentUserId, chat -> {
            // Abrir chat ao clicar
            Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
            intent.putExtra("otherUserId", chat.getOtherUserId(currentUserId));
            intent.putExtra("chatId", chat.getId());
            startActivity(intent);
        });
        recyclerChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerChats.setAdapter(adapter);
    }

    private void loadChats() {
        // Carregar chats onde o usuário atual é participante
        chatsRef.orderByChild("lastMessageTime")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatList.clear();
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            PrivateChat chat = chatSnapshot.getValue(PrivateChat.class);
                            if (chat != null && (currentUserId.equals(chat.getUser1Id()) ||
                                               currentUserId.equals(chat.getUser2Id()))) {
                                chat.setId(chatSnapshot.getKey());
                                chatList.add(chat);
                            }
                        }

                        // Ordenar por última mensagem (mais recente primeiro)
                        Collections.sort(chatList, (c1, c2) ->
                            Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));

                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateEmptyState() {
        if (chatList.isEmpty()) {
            recyclerChats.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerChats.setVisibility(View.VISIBLE);
            textEmptyState.setVisibility(View.GONE);
        }
    }
}
