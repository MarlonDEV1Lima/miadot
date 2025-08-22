package com.company.miadot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.adapters.ChatInboxAdapter;
import com.company.miadot.model.Mensagem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class ChatInboxActivity extends AppCompatActivity implements ChatInboxAdapter.OnChatClickListener {
    private RecyclerView recyclerChats;
    private ChatInboxAdapter adapter;
    private List<Mensagem> ultimasMensagens = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView textVazio;
    private Toolbar toolbar;
    private String userId;
    private DatabaseReference chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_inbox);

        initViews();
        setupToolbar();
        setupRecyclerView();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        carregarInbox();
    }

    private void initViews() {
        recyclerChats = findViewById(R.id.recyclerChats);
        progressBar = findViewById(R.id.progressBar);
        textVazio = findViewById(R.id.textVazio);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Configurar o clique do botão de voltar
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupRecyclerView() {
        adapter = new ChatInboxAdapter(ultimasMensagens, userId, this);
        recyclerChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerChats.setAdapter(adapter);
    }

    private void carregarInbox() {
        progressBar.setVisibility(View.VISIBLE);
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ultimasMensagens.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    Mensagem ultima = null;
                    for (DataSnapshot msgSnapshot : chatSnapshot.getChildren()) {
                        Mensagem m = msgSnapshot.getValue(Mensagem.class);
                        if (m != null && (m.getRemetenteId().equals(userId) || m.getDestinatarioId().equals(userId))) {
                            if (ultima == null || m.getTimestamp() > ultima.getTimestamp()) ultima = m;
                        }
                    }
                    if (ultima != null) ultimasMensagens.add(ultima);
                }
                Collections.sort(ultimasMensagens, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                textVazio.setVisibility(ultimasMensagens.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onChatClick(Mensagem ultimaMensagem) {
        String outroId = ultimaMensagem.getRemetenteId().equals(userId) ? ultimaMensagem.getDestinatarioId() : ultimaMensagem.getRemetenteId();
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("destinatarioId", outroId);
        startActivity(intent);
    }
}
