package com.company.miadot.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.adapters.ProfileAnimalAdapter;
import com.company.miadot.model.Animal;
import com.company.miadot.fragments.AnimalDetailDialogFragment; // Importar o DialogFragment
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import de.hdodenhof.circleimageview.CircleImageView;

// A interface OnAnimalClickListener agora está em seu próprio arquivo: OnAnimalClickListener.java
// Não precisa ser declarada aqui novamente.
public class ProfileActivity extends AppCompatActivity implements OnAnimalClickListener { // Implementa a interface

    private CircleImageView imageProfile;
    private TextView textName, textApelido;
    private RecyclerView recyclerViewAnimaisPerfil;

    private FirebaseAuth auth;
    private DatabaseReference usersRef, animaisRef;

    private List<Animal> animalList = new ArrayList<>();
    private ProfileAnimalAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialToolbar topAppBarProfile = findViewById(R.id.topAppBarProfile);
        topAppBarProfile.setNavigationOnClickListener(v -> finish());

        imageProfile = findViewById(R.id.imageProfile);
        textName = findViewById(R.id.textName);
        textApelido = findViewById(R.id.textApelido);
        recyclerViewAnimaisPerfil = findViewById(R.id.recyclerViewAnimaisPerfil);

        auth = FirebaseAuth.getInstance();

        // Pega o userId passado na Intent. Se não existir, usa o usuário atual.
        String userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            // Garante que o usuário atual não seja nulo antes de tentar obter o Uid
            if (auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
            } else {
                // Se não houver usuário logado e nenhum userId na intent,
                // você pode redirecionar para LoginActivity ou exibir um erro.
                // Por agora, vamos apenas mostrar um Toast e retornar.
                Toast.makeText(this, "Usuário não autenticado. Redirecionando para login.", Toast.LENGTH_LONG).show();
                // Exemplo: startActivity(new Intent(this, LoginActivity.class));
                finish();
                return; // Impede que o restante do onCreate continue com userId nulo
            }
        }


        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        animaisRef = FirebaseDatabase.getInstance().getReference("animais");

        setupRecyclerView();
        loadUserProfile();
        loadUserAnimals(userId);
    }


    private void setupRecyclerView() {
        // Passe 'this' (a ProfileActivity) como o listener para o adaptador
        adapter = new ProfileAnimalAdapter(this, animalList, this); // 'this' é a ProfileActivity, que implementa OnAnimalClickListener
        recyclerViewAnimaisPerfil.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewAnimaisPerfil.setAdapter(adapter);
    }

    private void loadUserAnimals(String userId) {
        animaisRef.orderByChild("donoId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        animalList.clear();
                        int count = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Animal animal = ds.getValue(Animal.class);
                            if (animal != null) {
                                animalList.add(animal);
                                count++;
                                Log.d("ProfileActivity", "Animal carregado: " + animal.getNome() + ", imageURL: " + animal.getImageURL());
                            }
                        }
                        Log.d("ProfileActivity", "Total de animais carregados: " + count);
                        adapter.notifyDataSetChanged();
                        if (count == 0) {
                            Toast.makeText(ProfileActivity.this, "Nenhum animal encontrado para este usuário.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Erro ao carregar animais.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadUserProfile() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nome = snapshot.child("fullName").getValue(String.class);
                    String apelido = snapshot.child("nickname").getValue(String.class);
                    String fotoUrl = snapshot.child("photoUrl").getValue(String.class);

                    textName.setText(nome != null ? nome : "Nome não disponível");
                    textApelido.setText(apelido != null ? "@" + apelido : "@apelido");

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this).load(fotoUrl).into(imageProfile);
                    } else {
                        // Define uma imagem de perfil padrão se não houver URL
                        imageProfile.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Perfil não encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Implementa o método da interface para lidar com o clique no animal
    @Override
    public void onAnimalClick(Animal animal) {
        // Cria uma nova instância do DialogFragment e a exibe
        AnimalDetailDialogFragment dialogFragment = AnimalDetailDialogFragment.newInstance(animal);
        dialogFragment.show(getSupportFragmentManager(), "AnimalDetailDialog");
    }
}
