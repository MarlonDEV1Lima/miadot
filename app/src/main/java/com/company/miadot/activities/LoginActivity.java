package com.company.miadot.activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.miadot.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString();



            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Tentando login com: " + email);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "Login bem-sucedido. UID: " + (user != null ? user.getUid() : "null"));
                            Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();

                            // Ir para tela principal
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(new Intent(LoginActivity.this, FeedActivity.class));
                            finish();
                        } else {
                            Exception e = task.getException();
                            if (e != null) {
                                Log.e(TAG, "Erro no login: " + e.getMessage(), e);
                                Toast.makeText(this, "Erro no login: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Log.e(TAG, "Erro desconhecido no login");
                                Toast.makeText(this, "Erro desconhecido no login", Toast.LENGTH_LONG).show();
                            }
                        }

                    });

        });

        findViewById(R.id.textViewCriarConta).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}
