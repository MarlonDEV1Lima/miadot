package com.company.miadot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.miadot.R;
import com.company.miadot.adapters.AnimalAdapter;
import com.company.miadot.model.Animal;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private List<Animal> animalList = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAnimais);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnimalAdapter(this, animalList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdicionar);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CadastrarAnimalActivity.class)));
        // Bottom menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_feed) {
                return true;
            } else if (itemId == R.id.profileImage) {
                startActivity(new Intent(this, PerfilActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, ConfiguracoesActivity.class));
                return true;
            }

            return false;
        });

        db = FirebaseFirestore.getInstance();
        carregarAnimais();
    }

    private void carregarAnimais() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("animais");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                animalList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Animal animal = child.getValue(Animal.class);
                    if (animal != null) {
                        // Setar o id do animal com a key do snapshot (ID do nó no Realtime Database)
                        animal.setId(child.getKey());
                        animalList.add(animal);
                    }
                }
                Log.d("FeedActivity", "Número de animais: " + animalList.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FeedActivity", "Erro ao carregar animais", error.toException());
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
    }
}
