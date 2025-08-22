package com.company.miadot.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.adapters.NotificacaoAdapter;
import com.company.miadot.model.Notificacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class NotificationCenterActivity extends AppCompatActivity implements NotificacaoAdapter.OnNotificacaoClickListener {
    private RecyclerView recyclerView;
    private NotificacaoAdapter adapter;
    private List<Notificacao> notificacoes = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView textVazio;
    private DatabaseReference notificacoesRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);
        recyclerView = findViewById(R.id.recyclerNotificacoes);
        progressBar = findViewById(R.id.progressBar);
        textVazio = findViewById(R.id.textVazio);
        adapter = new NotificacaoAdapter(notificacoes, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificacoesRef = FirebaseDatabase.getInstance().getReference("notificacoes").child(userId);
        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        progressBar.setVisibility(View.VISIBLE);
        notificacoesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificacoes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Notificacao n = ds.getValue(Notificacao.class);
                    if (n != null) notificacoes.add(0, n);
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                textVazio.setVisibility(notificacoes.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onNotificacaoClick(Notificacao notificacao) {
        notificacao.setLida(true);
        notificacoesRef.child(notificacao.getId()).child("lida").setValue(true);
        // Ações rápidas: abrir post, perfil, aceitar interesse, etc (implementar conforme tipo)
    }
}

