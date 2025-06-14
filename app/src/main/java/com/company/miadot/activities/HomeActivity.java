package com.company.miadot.activities;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.company.miadot.R;

public class HomeActivity extends AppCompatActivity {

    private Button btnVerPets, btnCadastrarPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnVerPets = findViewById(R.id.btnVerPets);
        btnCadastrarPet = findViewById(R.id.btnCadastrarPet);

        btnVerPets.setOnClickListener(v -> {
            // Aqui você pode futuramente abrir a tela de listagem de pets
            // startActivity(new Intent(this, ListaPetsActivity.class));
        });

        btnCadastrarPet.setOnClickListener(v -> {
            // Aqui você pode futuramente abrir a tela de cadastro de pet
            // startActivity(new Intent(this, CadastroPetActivity.class));
        });
    }
}
