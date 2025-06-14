package com.company.miadot.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity; // ✅ Importa a classe correta

import com.company.miadot.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity { // ✅ Herda da classe certa

    private static final String TAG = "MainActivity";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        if (auth != null) {
            Log.d(TAG, "✅ Firebase conectado com sucesso!");
        } else {
            Log.e(TAG, "❌ Erro ao conectar com Firebase.");
        }


    }

}

