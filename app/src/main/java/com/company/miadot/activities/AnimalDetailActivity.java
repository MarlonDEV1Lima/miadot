package com.company.miadot.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.company.miadot.R;

public class AnimalDetailActivity extends AppCompatActivity {

    private ImageView imageAnimalDetail;
    private TextView textNomeAnimal, textDescricao, textIdade, textEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_detail);

        imageAnimalDetail = findViewById(R.id.imageAnimalDetail);
        textNomeAnimal = findViewById(R.id.textNomeAnimal);
        textDescricao = findViewById(R.id.textDescricao);
        textIdade = findViewById(R.id.textIdade);
        textEstado = findViewById(R.id.textEstado);

        // Recebendo dados via Intent
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String nome = getIntent().getStringExtra("nome");
        String descricao = getIntent().getStringExtra("descricao");
        String idade = getIntent().getStringExtra("idade");
        String estado = getIntent().getStringExtra("estado");

        // Preenchendo a tela
        Glide.with(this).load(imageUrl).into(imageAnimalDetail);
        textNomeAnimal.setText(nome);
        textDescricao.setText(descricao);
        textIdade.setText("Idade: " + idade);
        textEstado.setText("Estado: " + estado);
    }
}
