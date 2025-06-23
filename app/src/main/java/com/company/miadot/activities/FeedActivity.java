package com.company.miadot.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.adapters.AnimalAdapter;
import com.company.miadot.model.Animal;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        topAppBar.setBackgroundColor(getResources().getColor(android.R.color.white));
        topAppBar.setTitleTextColor(getResources().getColor(android.R.color.black));
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_notifications) {
                // TODO: implementar ação notificações
                return true;
            } else if (item.getItemId() == R.id.action_messages) {
                // TODO: implementar ação mensagens
                return true;
            }
            return false;
        });

        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) {
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, CadastrarAnimalActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, ConfiguracoesActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;

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

        // FloatingActionButton para adicionar animal
        FloatingActionButton fab = findViewById(R.id.fabAdicionar);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CadastrarAnimalActivity.class)));

        // Configura foto de perfil flutuante
        configurarFotoPerfil();
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

    // Configura a foto de perfil flutuante no canto inferior direito
    private void configurarFotoPerfil() {
        ImageView imageProfile = findViewById(R.id.imageProfile);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.child("photoUrl").getValue() != null) {
                        String fotoUrl = snapshot.child("photoUrl").getValue(String.class);
                        Log.d("FeedActivity", "FotoUrl do usuário: " + fotoUrl);

                        Glide.with(FeedActivity.this)
                                .load(fotoUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .circleCrop()
                                .into(imageProfile);
                    } else {
                        Log.d("FeedActivity", "FotoUrl não encontrada, usando padrão");
                        imageProfile.setImageResource(R.drawable.default_profile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FeedActivity", "Erro ao carregar foto do usuário", error.toException());
                    imageProfile.setImageResource(R.drawable.default_profile);
                }
            });
        } else {
            // Usuário não logado, usa imagem padrão
            imageProfile.setImageResource(R.drawable.default_profile);
        }

        // Clique na foto do perfil abre tela de perfil
        imageProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, PerfilActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remova listeners se necessário (no seu caso, não há listener global)
    }
}
