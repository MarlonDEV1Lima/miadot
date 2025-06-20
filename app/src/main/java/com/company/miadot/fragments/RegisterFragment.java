package com.company.miadot.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.company.miadot.R;
import com.company.miadot.activities.FeedActivity;
import com.company.miadot.model.CepResponse;
import com.company.miadot.services.ViaCepService;
import com.company.miadot.utils.CloudinaryConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private ViaCepService viaCepService;

    private Uri selectedImageUri = null;
    private ImageView imageViewProfile;
    private static final int PICK_IMAGE_REQUEST = 1;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        EditText fullName = view.findViewById(R.id.editTextRegisterFullName);
        EditText nickname = view.findViewById(R.id.editTextRegisterNickname);
        EditText email = view.findViewById(R.id.editTextRegisterEmail);
        EditText password = view.findViewById(R.id.editTextRegisterPassword);
        EditText cep = view.findViewById(R.id.editTextRegisterCep);
        EditText street = view.findViewById(R.id.editTextRegisterStreet);
        EditText neighborhood = view.findViewById(R.id.editTextRegisterNeighborhood);
        EditText city = view.findViewById(R.id.editTextRegisterCity);
        EditText state = view.findViewById(R.id.editTextRegisterState);
        Button registerButton = view.findViewById(R.id.buttonRegister);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        Button buttonSelectImage = view.findViewById(R.id.buttonSelectImage);

        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://viacep.com.br/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        viaCepService = retrofit.create(ViaCepService.class);

        cep.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String cepStr = s.toString().replaceAll("[^0-9]", "");
                if (cepStr.length() == 8) {
                    buscarEnderecoPorCep(cepStr, street, neighborhood, city, state);
                }
            }
        });

        registerButton.setOnClickListener(v -> {
            String fullNameStr = fullName.getText().toString().trim();
            String nicknameStr = nickname.getText().toString().trim();
            String emailStr = email.getText().toString().trim();
            String passStr = password.getText().toString().trim();
            String cepStr = cep.getText().toString().trim();
            String streetStr = street.getText().toString().trim();
            String neighborhoodStr = neighborhood.getText().toString().trim();
            String cityStr = city.getText().toString().trim();
            String stateStr = state.getText().toString().trim();


            if (fullNameStr.isEmpty() || nicknameStr.isEmpty() || emailStr.isEmpty() || passStr.isEmpty() ||
                    cepStr.isEmpty() || streetStr.isEmpty() || neighborhoodStr.isEmpty() ||
                    cityStr.isEmpty() || stateStr.isEmpty() || selectedImageUri == null) {
                Toast.makeText(getContext(), "Preencha todos os campos e selecione uma imagem", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                new Thread(() -> {
                    try {
                        Map uploadResult = CloudinaryConfig.uploadImage(imageBytes);
                        String imageUrl = uploadResult.get("secure_url").toString();

                        getActivity().runOnUiThread(() -> {
                            mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String uid = mAuth.getCurrentUser().getUid();

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("fullName", fullNameStr);
                                            userData.put("nickname", nicknameStr);
                                            userData.put("email", emailStr);
                                            userData.put("photoUrl", imageUrl);
                                            userData.put("cep", cepStr);
                                            userData.put("street", streetStr);
                                            userData.put("neighborhood", neighborhoodStr);
                                            userData.put("city", cityStr);
                                            userData.put("state", stateStr);

                                            database.child("users").child(uid).setValue(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(getContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getActivity(), FeedActivity.class));
                                                        getActivity().finish();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_LONG).show());

                                        } else {
                                            Toast.makeText(getContext(), "Erro no cadastro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        });
                    } catch (Exception e) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Erro ao enviar imagem: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start();

            } catch (IOException e) {
                Toast.makeText(getContext(), "Erro ao processar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageViewProfile.setImageURI(selectedImageUri);
        }
    }

    private void buscarEnderecoPorCep(String cep, EditText street, EditText neighborhood, EditText city, EditText state) {
        Call<CepResponse> call = viaCepService.buscarCep(cep);
        call.enqueue(new Callback<CepResponse>() {
            @Override
            public void onResponse(Call<CepResponse> call, Response<CepResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCep() != null) {
                    CepResponse cepResponse = response.body();
                    street.setText(cepResponse.getLogradouro());
                    neighborhood.setText(cepResponse.getBairro());
                    city.setText(cepResponse.getLocalidade());
                    state.setText(cepResponse.getUf());
                } else {
                    Toast.makeText(getContext(), "CEP não encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CepResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Erro ao buscar CEP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
