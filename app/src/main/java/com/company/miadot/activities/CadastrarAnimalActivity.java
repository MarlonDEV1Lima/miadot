package com.company.miadot.activities;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.company.miadot.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.company.miadot.model.Animal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

public class CadastrarAnimalActivity extends AppCompatActivity {

    private Uri imagemSelecionada;
    private ImageView imageAnimal;
    private EditText etNome, etIdade, etEstado, etDescricao;
    private DatabaseReference databaseRef;
    private boolean uploadFinalizado = false; // flag de controle
    private Button btnCadastrar;

    private byte[] getBytesFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_animal);

        imageAnimal = findViewById(R.id.imageAnimal);
        etNome = findViewById(R.id.etNome);
        etIdade = findViewById(R.id.etIdade);
        etEstado = findViewById(R.id.etEstado);
        etDescricao = findViewById(R.id.etDescricao);
        Button btnSelecionarImagem = findViewById(R.id.btnSelecionarImagem);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        databaseRef = FirebaseDatabase.getInstance().getReference("animais");

        btnSelecionarImagem.setOnClickListener(v -> selecionarImagem());
        btnCadastrar.setOnClickListener(v -> cadastrarAnimal());
    }

    private void selecionarImagem() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imagemSelecionada = data.getData();
            imageAnimal.setImageURI(imagemSelecionada);
        }
    }

    private void cadastrarAnimal() {
        String nome = etNome.getText().toString().trim();
        String idade = etIdade.getText().toString().trim();
        String estado = etEstado.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getUid();

        if (imagemSelecionada != null && !nome.isEmpty()) {
            btnCadastrar.setEnabled(false); // desativa o botão
            uploadFinalizado = false;       // reseta flag

            try {
                byte[] imageBytes = getBytesFromUri(imagemSelecionada);

                MediaManager.get().upload(imageBytes)
                        .option("resource_type", "image")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {}

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {}

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                if (uploadFinalizado) return;
                                uploadFinalizado = true;

                                String imageUrl = resultData.get("secure_url").toString();

                                Animal animal = new Animal();
                                animal.setNome(nome);
                                animal.setIdade(idade);
                                animal.setEstado(estado);
                                animal.setDescricao(descricao);
                                animal.setImageURL(imageUrl);
                                animal.setDonoId(userId);
                                animal.setLikes(0);
                                animal.setInteressados(0);

                                String id = databaseRef.push().getKey();
                                if (id != null) {
                                    animal.setId(id);
                                    databaseRef.child(id).setValue(animal)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(CadastrarAnimalActivity.this, "Animal cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                btnCadastrar.setEnabled(true); // reativa botão em caso de falha
                                                Toast.makeText(CadastrarAnimalActivity.this, "Erro ao cadastrar animal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                } else {
                                    btnCadastrar.setEnabled(true);
                                    Toast.makeText(CadastrarAnimalActivity.this, "Erro ao gerar ID do animal", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                btnCadastrar.setEnabled(true);
                                Toast.makeText(CadastrarAnimalActivity.this, "Erro ao enviar imagem: " + error.getDescription(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {}
                        }).dispatch();

            } catch (Exception e) {
                btnCadastrar.setEnabled(true);
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Preencha os campos e selecione uma imagem", Toast.LENGTH_SHORT).show();
        }
    }
}
