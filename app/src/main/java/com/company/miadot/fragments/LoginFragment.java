package com.company.miadot.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.company.miadot.R;
import com.company.miadot.activities.FeedActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = view.findViewById(R.id.etEmail);
        editTextPassword = view.findViewById(R.id.etPassword);
        Button buttonLogin = view.findViewById(R.id.btnLogin);

        // ✅ Validação do email com ícone de check
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    editTextEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_degrade, 0);
                } else {
                    editTextEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 👁 Mostrar/Ocultar senha com clique no drawableEnd
        editTextPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTextPassword.getRight() -
                        editTextPassword.getCompoundDrawables()[DRAWABLE_END].getBounds().width() - editTextPassword.getPaddingEnd())) {

                    int selection = editTextPassword.getSelectionEnd();

                    boolean isPasswordHidden = (editTextPassword.getInputType() ==
                            (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));

                    if (isPasswordHidden) {
                        // Mostrar senha (olho aberto)
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
                    } else {
                        // Ocultar senha (olho fechado)
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                    }

                    editTextPassword.setSelection(selection);
                    return true;
                }
            }
            return false;
        });


        // 🔐 Login
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getActivity(), FeedActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Erro no login: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
