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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView imageProfile;
    private TextView textNomeUsuario, textApelido, textContagemAnimais;
    private RecyclerView recyclerViewAnimaisPerfil;

    private FirebaseAuth auth;
    private DatabaseReference usersRef, animaisRef;

    private List<Animal> animalList = new ArrayList<>();
    private ProfileAnimalAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageProfile = findViewById(R.id.imageProfile);
        textNomeUsuario = findViewById(R.id.textNomeUsuario);
        textApelido = findViewById(R.id.textApelido);
        textContagemAnimais = findViewById(R.id.textContagemAnimais);
        recyclerViewAnimaisPerfil = findViewById(R.id.recyclerViewAnimaisPerfil);

        auth = FirebaseAuth.getInstance();

        // Pega o userId passado na Intent. Se não existir, usa o usuário atual.
        String userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            userId = auth.getCurrentUser().getUid();
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        animaisRef = FirebaseDatabase.getInstance().getReference("animais");

        setupRecyclerView();
        loadUserProfile();
        loadUserAnimals(userId);
    }


    private void setupRecyclerView() {
        adapter = new ProfileAnimalAdapter(this, animalList);
        recyclerViewAnimaisPerfil.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewAnimaisPerfil.setAdapter(adapter);
    }

    private void loadUserAnimals(String userId) {
        animaisRef.orderByChild("donoId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        animalList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Animal animal = ds.getValue(Animal.class);
                            if (animal != null) {
                                animalList.add(animal);
                            }
                        }
                        textContagemAnimais.setText(animalList.size() + " animais cadastrados");
                        adapter.notifyDataSetChanged();
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
                    String nome = snapshot.child("fullName").getValue(String.class); // CORRETO
                    String apelido = snapshot.child("nickname").getValue(String.class); // CORRETO
                    String fotoUrl = snapshot.child("photoUrl").getValue(String.class); // CORRETO

                    textNomeUsuario.setText(nome != null ? nome : "Nome não disponível");
                    textApelido.setText(apelido != null ? "@" + apelido : "@apelido");

                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this).load(fotoUrl).into(imageProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
