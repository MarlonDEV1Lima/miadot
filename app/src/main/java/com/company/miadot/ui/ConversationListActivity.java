package com.company.miadot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.company.miadot.R;
import com.company.miadot.adapters.ConversationAdapter;
import com.company.miadot.model.Conversation;
import com.company.miadot.activities.ChatActivity;
import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private String currentUserId = "usuario_atual"; // Substitua pelo ID real do usuário logado

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        recyclerView = findViewById(R.id.recyclerViewConversations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Atualiza currentUserId para o usuário logado real
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        adapter = new ConversationAdapter(conversationList, conversation -> {
            // Chat privado: identificar o outro usuário
            String otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("otherUserId", otherUserId); // Corrigido para usar 'otherUserId'
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // FAB para nova mensagem
        findViewById(R.id.fabNewMessage).setOnClickListener(v -> {
            // Esta funcionalidade precisa ser implementada com seleção de usuário
            // Por enquanto, vamos desabilitar para evitar erros
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
            // TODO: Implementar seleção de usuário para nova conversa
        });

        // Carrega conversas reais do Firebase
        loadAllConversations();
        configurarBadgeMensagens();
    }

    private void loadAllConversations() {
        conversationList.clear();
        DatabaseReference conversasRef = FirebaseDatabase.getInstance().getReference("conversas");
        conversasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversationList.clear();
                for (DataSnapshot convSnap : snapshot.getChildren()) {
                    Conversation conv = convSnap.getValue(Conversation.class);
                    if (conv != null && currentUserId != null &&
                        (currentUserId.equals(conv.getUser1Id()) || currentUserId.equals(conv.getUser2Id()))) {
                        conversationList.add(conv);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @androidx.annotation.OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
    private void configurarBadgeMensagens() {
        com.google.android.material.appbar.MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        com.google.android.material.badge.BadgeDrawable badgeMensagens = com.google.android.material.badge.BadgeDrawable.create(this);
        badgeMensagens.setBackgroundColor(getColor(R.color.red));
        badgeMensagens.setBadgeGravity(com.google.android.material.badge.BadgeDrawable.TOP_END);
        badgeMensagens.setHorizontalOffset(20);
        badgeMensagens.setVerticalOffset(10);
        badgeMensagens.setVisible(false);
        com.google.android.material.badge.BadgeUtils.attachBadgeDrawable(badgeMensagens, topAppBar, 0); // 0 = sem menu, badge no título

        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference notificacoesRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("notificacoes")
                .child(userId);
        notificacoesRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                long mensagensNaoLidas = 0;
                if (snapshot.child("mensagensPendentes").exists()) {
                    mensagensNaoLidas = snapshot.child("mensagensPendentes").getChildrenCount();
                }
                badgeMensagens.setVisible(mensagensNaoLidas > 0);
                badgeMensagens.setNumber((int) mensagensNaoLidas);
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
}
