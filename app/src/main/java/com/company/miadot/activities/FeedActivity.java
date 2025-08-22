package com.company.miadot.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.miadot.R;
import com.company.miadot.adapters.AnimalAdapter;
import com.company.miadot.model.Animal;
import com.company.miadot.ui.ConversationListActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private List<Animal> animalList = new ArrayList<>();
    private ShimmerFrameLayout shimmerFrameLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String lastKey = null;
    private static final int PAGE_SIZE = 10;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Bind views
        shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewAnimais);

        // Configurar FAB de mensagens
        FloatingActionButton fabMessages = findViewById(R.id.fabMessages);
        TextView badgeMessages = findViewById(R.id.badgeMessages);

        fabMessages.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, MessagesActivity.class);
            startActivity(intent);
        });

        // Configurar monitoramento de mensagens não lidas
        setupUnreadMessagesBadge(badgeMessages);

        // TESTE: Forçar exibição do badge para testar o posicionamento
        // Remova esta linha depois de confirmar que o badge aparece
        updateMessagesBadge(badgeMessages, 3);

        // Inicializar shimmer e esconder RecyclerView até carregar dados
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // Configurar RecyclerView e Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalAdapter(this, animalList);
        recyclerView.setAdapter(adapter);

        // Swipe para atualizar lista
        swipeRefreshLayout.setOnRefreshListener(() -> {
            resetPagination();
            loadAnimalsPaginated();
        });

        // Configurar Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        topAppBar.setBackgroundColor(getResources().getColor(android.R.color.white));
        topAppBar.setTitleTextColor(getResources().getColor(android.R.color.black));

        // Configurar navegação inferior customizada
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navMessages = findViewById(R.id.nav_messages);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            // Já estamos na home, não fazer nada
        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastrarAnimalActivity.class));
        });

        navMessages.setOnClickListener(v -> {
            startActivity(new Intent(this, MessagesActivity.class));
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Carregar dados paginados inicialmente
        loadAnimalsPaginated();


        // Scroll Listener para carregar mais dados quando chegar no final
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) return; // Não processa scroll para cima

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadAnimalsPaginated();
                    }
                }
            }
        });

        // FloatingActionButton removido - usar apenas o botão do menu inferior
    }

    // Método para resetar paginação e lista
    private void resetPagination() {
        lastKey = null;
        isLastPage = false;
        animalList.clear();
        adapter.notifyDataSetChanged();
    }

    // Método que carrega animais com paginação do Firebase Realtime Database
    private void loadAnimalsPaginated() {
        isLoading = true;
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("animais");
        Query query = (lastKey == null) ? ref.orderByKey().limitToFirst(PAGE_SIZE)
                : ref.orderByKey().startAfter(lastKey).limitToFirst(PAGE_SIZE);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Animal animal = child.getValue(Animal.class);
                    if (animal != null) {
                        animal.setId(child.getKey());
                        animalList.add(animal);
                        lastKey = child.getKey();
                        count++;
                    }
                }

                adapter.notifyDataSetChanged();
                isLoading = false;
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);

                if (count < PAGE_SIZE) {
                    isLastPage = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading = false;
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                Log.e("FeedActivity", "Erro na paginação", error.toException());
            }
        });
    }

    // Configura monitoramento de mensagens não lidas para o badge
    private void setupUnreadMessagesBadge(TextView badgeMessages) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            badgeMessages.setVisibility(View.GONE);
            return;
        }

        // Monitorar conversas onde o usuário tem mensagens não lidas
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("private_chats");
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalUnreadMessages = 0;

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    try {
                        // Verificar se o usuário atual participa da conversa
                        String user1Id = chatSnapshot.child("user1Id").getValue(String.class);
                        String user2Id = chatSnapshot.child("user2Id").getValue(String.class);
                        String chatId = chatSnapshot.getKey();

                        if (currentUserId.equals(user1Id)) {
                            // Usuário é user1, verificar se tem mensagens não lidas
                            Boolean hasUnread = chatSnapshot.child("user1HasUnread").getValue(Boolean.class);
                            if (hasUnread != null && hasUnread && chatId != null) {
                                // Contar mensagens não lidas nesta conversa
                                countUnreadMessagesInChat(chatId, currentUserId, count -> {
                                    // Esta callback será chamada de forma assíncrona
                                    // Vamos usar uma abordagem diferente
                                });
                                // Por enquanto, contar como 1 conversa não lida
                                totalUnreadMessages++;
                            }
                        } else if (currentUserId.equals(user2Id)) {
                            // Usuário é user2, verificar se tem mensagens não lidas
                            Boolean hasUnread = chatSnapshot.child("user2HasUnread").getValue(Boolean.class);
                            if (hasUnread != null && hasUnread && chatId != null) {
                                totalUnreadMessages++;
                            }
                        }
                    } catch (Exception e) {
                        // Ignorar erros de conversão
                        Log.e("FeedActivity", "Erro ao processar chat: " + e.getMessage());
                    }
                }

                // Criar variável final para uso na lambda
                final int finalUnreadCount = totalUnreadMessages;
                // Atualizar badge na UI thread
                runOnUiThread(() -> updateMessagesBadge(badgeMessages, finalUnreadCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FeedActivity", "Erro ao monitorar mensagens não lidas: " + error.getMessage());
                runOnUiThread(() -> badgeMessages.setVisibility(View.GONE));
            }
        });
    }

    // Método auxiliar para contar mensagens não lidas em uma conversa específica
    private void countUnreadMessagesInChat(String chatId, String currentUserId, UnreadCountCallback callback) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);
        messagesRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    try {
                        String senderId = messageSnapshot.child("remetenteId").getValue(String.class);
                        Boolean isRead = messageSnapshot.child("visualizada").getValue(Boolean.class);

                        // Contar mensagens de outros usuários que não foram lidas
                        if (senderId != null && !senderId.equals(currentUserId) &&
                                (isRead == null || !isRead)) {
                            unreadCount++;
                        }
                    } catch (Exception e) {
                        Log.e("FeedActivity", "Erro ao processar mensagem: " + e.getMessage());
                    }
                }
                callback.onCountReceived(unreadCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCountReceived(0);
            }
        });
    }

    // Interface para callback de contagem
    private interface UnreadCountCallback {
        void onCountReceived(int count);
    }

    // Atualiza o visual do badge de mensagens
    private void updateMessagesBadge(TextView badgeMessages, int unreadCount) {
        if (unreadCount > 0) {
            badgeMessages.setVisibility(View.VISIBLE);
            if (unreadCount > 99) {
                badgeMessages.setText("99+");
            } else {
                badgeMessages.setText(String.valueOf(unreadCount));
            }
        } else {
            badgeMessages.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remova listeners se necessário (no seu caso, não há listener global)
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_messages) {
            startActivity(new Intent(this, ConversationListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
