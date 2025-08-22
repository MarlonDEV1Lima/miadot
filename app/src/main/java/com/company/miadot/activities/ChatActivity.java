package com.company.miadot.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.company.miadot.R;
import com.company.miadot.adapters.MensagemAdapter;
import com.company.miadot.model.Mensagem;
import com.company.miadot.model.PrivateChat;
import com.company.miadot.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerMensagens;
    private EditText editMensagem;
    private ImageButton buttonEnviar;
    private TextView toolbarTitle;
    private Toolbar toolbar;
    private MensagemAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private DatabaseReference mensagensRef, chatsRef;
    private String currentUserId, otherUserId, chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupToolbar();

        // Só continua se conseguir obter dados válidos do Intent
        if (!getIntentData()) {
            return; // Se falhou, a activity já foi finalizada
        }

        setupRecyclerView();
        setupChatReference();
        loadOtherUserInfo();
        loadMessages();
        setupSendButton();
    }

    private void initViews() {
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        editMensagem = findViewById(R.id.editMensagem);
        buttonEnviar = findViewById(R.id.buttonEnviar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbar = findViewById(R.id.toolbar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                       FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
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

    private boolean getIntentData() {
        otherUserId = getIntent().getStringExtra("otherUserId");
        chatId = getIntent().getStringExtra("chatId");

        // Validação do usuário atual
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Erro: usuário não autenticado", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        // Validação do destinatário
        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "Erro: destinatário inválido", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        // Validação: usuário não pode conversar consigo mesmo
        if (currentUserId.equals(otherUserId)) {
            Toast.makeText(this, "Erro: não é possível conversar consigo mesmo", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        // Se não foi passado um chatId, criar um novo
        if (chatId == null || chatId.isEmpty()) {
            chatId = generateChatId(currentUserId, otherUserId);
        }

        // Validação final crítica do chatId
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Erro crítico: não foi possível gerar ID do chat", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        return true; // Sucesso
    }

    private void setupRecyclerView() {
        adapter = new MensagemAdapter(mensagens, currentUserId);
        recyclerMensagens.setLayoutManager(new LinearLayoutManager(this));
        recyclerMensagens.setAdapter(adapter);
    }

    private void setupChatReference() {
        mensagensRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);
        chatsRef = FirebaseDatabase.getInstance().getReference("private_chats").child(chatId);
    }

    private void loadOtherUserInfo() {
        FirebaseDatabase.getInstance().getReference("usuarios").child(otherUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User usuario = snapshot.getValue(User.class);
                        if (usuario != null) {
                            String displayName = usuario.getNickname() != null ?
                                usuario.getNickname() : usuario.getFullName();
                            toolbarTitle.setText(displayName != null ? displayName : "Usuário");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadMessages() {
        mensagensRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mensagens.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Mensagem m = ds.getValue(Mensagem.class);
                    if (m != null) {
                        mensagens.add(m);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!mensagens.isEmpty()) {
                    recyclerMensagens.scrollToPosition(mensagens.size() - 1);
                }

                // Marcar mensagens como lidas
                markMessagesAsRead();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSendButton() {
        buttonEnviar.setOnClickListener(v -> sendMessage());

        editMensagem.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String texto = editMensagem.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        String messageId = mensagensRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        Mensagem mensagem = new Mensagem(messageId, currentUserId, otherUserId,
                                       texto, "texto", null, timestamp, false);

        // Enviar mensagem
        mensagensRef.child(messageId).setValue(mensagem)
                .addOnSuccessListener(aVoid -> {
                    editMensagem.setText("");
                    updateChatLastMessage(texto, timestamp);
                    // Enviar notificação para o outro usuário
                    sendNotificationToOtherUser(texto);
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, R.string.erro_enviar_mensagem, Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToOtherUser(String messageText) {
        // Criar notificação para o destinatário
        DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                .getReference("user_notifications")
                .child(otherUserId)
                .push();

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "new_message");
        notification.put("senderId", currentUserId);
        notification.put("senderName", ""); // Será preenchido abaixo
        notification.put("message", messageText);
        notification.put("chatId", chatId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        // Buscar nome do remetente
        FirebaseDatabase.getInstance().getReference("usuarios").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        String senderName = "Usuário";
                        if (user != null) {
                            senderName = user.getNickname() != null ? user.getNickname() : user.getFullName();
                        }
                        notification.put("senderName", senderName != null ? senderName : "Usuário");
                        notificationRef.setValue(notification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        notification.put("senderName", "Usuário");
                        notificationRef.setValue(notification);
                    }
                });
    }

    private void updateChatLastMessage(String lastMessage, long timestamp) {
        // Verificar se o chat já existe, se não, criar
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PrivateChat chat;
                if (snapshot.exists()) {
                    chat = snapshot.getValue(PrivateChat.class);
                } else {
                    chat = new PrivateChat(currentUserId, otherUserId, null);
                }

                if (chat != null) {
                    chat.setLastMessage(lastMessage);
                    chat.setLastMessageTime(timestamp);
                    chat.setLastMessageSenderId(currentUserId);

                    // Marcar como não lida para o destinatário
                    if (currentUserId.equals(chat.getUser1Id())) {
                        chat.setUser2HasUnread(true);
                    } else {
                        chat.setUser1HasUnread(true);
                    }

                    chatsRef.setValue(chat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void markMessagesAsRead() {
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PrivateChat chat = snapshot.getValue(PrivateChat.class);
                if (chat != null) {
                    // Marcar como lida para o usuário atual
                    if (currentUserId.equals(chat.getUser1Id())) {
                        chat.setUser1HasUnread(false);
                    } else {
                        chat.setUser2HasUnread(false);
                    }
                    chatsRef.setValue(chat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String generateChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
