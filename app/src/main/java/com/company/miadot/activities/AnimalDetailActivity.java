package com.company.miadot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.model.Animal;
import com.company.miadot.model.PrivateChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AnimalDetailActivity extends AppCompatActivity {

    private ImageView imageAnimalDetail;
    private TextView textNomeAnimal, textDescricao, textIdade, textEstado;
    private Button buttonTenhoInteresse;
    private Animal animal;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_detail);

        initViews();
        loadAnimalData();
        setupTenhoInteresseButton();
    }

    private void initViews() {
        imageAnimalDetail = findViewById(R.id.imageAnimalDetail);
        textNomeAnimal = findViewById(R.id.textNomeAnimal);
        textDescricao = findViewById(R.id.textDescricao);
        textIdade = findViewById(R.id.textIdade);
        textEstado = findViewById(R.id.textEstado);
        buttonTenhoInteresse = findViewById(R.id.buttonTenhoInteresse);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void loadAnimalData() {
        // Recebendo dados via Intent
        String animalId = getIntent().getStringExtra("animalId");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String nome = getIntent().getStringExtra("nome");
        String descricao = getIntent().getStringExtra("descricao");
        String idade = getIntent().getStringExtra("idade");
        String estado = getIntent().getStringExtra("estado");
        String donoId = getIntent().getStringExtra("donoId");

        // Criar objeto animal temporário
        animal = new Animal();
        animal.setId(animalId);
        animal.setNome(nome);
        animal.setDescricao(descricao);
        animal.setIdade(idade);
        animal.setEstado(estado);
        animal.setDonoId(donoId);
        animal.setImageURL(imageUrl);

        // Preenchendo a tela
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(imageAnimalDetail);
        }
        textNomeAnimal.setText(nome != null ? nome : "Nome não disponível");
        textDescricao.setText(descricao != null ? descricao : "Descrição não disponível");
        textIdade.setText("Idade: " + (idade != null ? idade : "Não informada"));
        textEstado.setText("Estado: " + (estado != null ? estado : "Não informado"));

        // Verificar se é o próprio dono do animal ou se donoId é inválido
        if (currentUserId != null && currentUserId.equals(donoId)) {
            buttonTenhoInteresse.setText("Meu Animal");
            buttonTenhoInteresse.setEnabled(false);
        } else if (donoId == null || donoId.isEmpty()) {
            buttonTenhoInteresse.setText("Dono não identificado");
            buttonTenhoInteresse.setEnabled(false);
        }
    }

    private void setupTenhoInteresseButton() {
        buttonTenhoInteresse.setOnClickListener(v -> {
            if (animal == null) {
                Toast.makeText(this, "Erro: dados do animal não carregados", Toast.LENGTH_SHORT).show();
                return;
            }

            String donoId = animal.getDonoId();
            if (donoId == null || donoId.isEmpty()) {
                Toast.makeText(this, "Erro: dono do animal não identificado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUserId == null || currentUserId.isEmpty()) {
                Toast.makeText(this, "Erro: você precisa estar logado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUserId.equals(donoId)) {
                Toast.makeText(this, "Este é seu próprio animal", Toast.LENGTH_SHORT).show();
                return;
            }

            createOrOpenChat(donoId, animal.getId());
        });
    }

    private void createOrOpenChat(String donoId, String animalId) {
        if (currentUserId == null || donoId == null) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId.equals(donoId)) {
            Toast.makeText(this, "Você não pode conversar com você mesmo", Toast.LENGTH_SHORT).show();
            return;
        }

        String chatId = generateChatId(currentUserId, donoId);
        DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("private_chats").child(chatId);

        // Verificar se o chat já existe
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PrivateChat chat;
                if (snapshot.exists()) {
                    chat = snapshot.getValue(PrivateChat.class);
                } else {
                    // Criar novo chat
                    chat = new PrivateChat(currentUserId, donoId, animalId);
                    chat.setLastMessage(getString(R.string.interesse_animal));
                    chat.setLastMessageTime(System.currentTimeMillis());
                    chat.setLastMessageSenderId(currentUserId);

                    // Marcar como não lida para o dono
                    if (currentUserId.equals(chat.getUser1Id())) {
                        chat.setUser2HasUnread(true);
                    } else {
                        chat.setUser1HasUnread(true);
                    }

                    chatRef.setValue(chat);
                }

                // Abrir o chat
                Intent intent = new Intent(AnimalDetailActivity.this, ChatActivity.class);
                intent.putExtra("otherUserId", donoId);
                intent.putExtra("chatId", chatId);
                startActivity(intent);

                Toast.makeText(AnimalDetailActivity.this,
                        R.string.conversa_iniciada, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnimalDetailActivity.this,
                        "Erro ao criar conversa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
