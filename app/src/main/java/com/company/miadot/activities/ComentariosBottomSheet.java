package com.company.miadot.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.adapters.ComentarioAdapter;
import com.company.miadot.model.Comentarios;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ComentariosBottomSheet extends BottomSheetDialogFragment {

    private String animalId;
    private RecyclerView recyclerComentarios;
    private EditText editComentario;
    private Button buttonPublicar;
    private ComentarioAdapter adapter;
    private List<Comentarios> listaComentarios = new ArrayList<>();
    private String respondendoParaComentarioId = null;

    private int comentariosVisiveis = 10;
    private TextView verMaisComentarios;

    public static ComentariosBottomSheet novaInstancia(String animalId) {
        ComentariosBottomSheet fragment = new ComentariosBottomSheet();
        Bundle args = new Bundle();
        args.putString("animalId", animalId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_comentarios, container, false);

        recyclerComentarios = view.findViewById(R.id.recyclerComentariosBottom);
        editComentario = view.findViewById(R.id.editNovoComentario);
        buttonPublicar = view.findViewById(R.id.buttonPublicarComentario);
        verMaisComentarios = view.findViewById(R.id.verMaisComentarios);

        if (getArguments() != null) {
            animalId = getArguments().getString("animalId");
        }

        recyclerComentarios.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ComentarioAdapter(requireContext(), listaComentarios, false);
        recyclerComentarios.setAdapter(adapter);

        adapter.setOnResponderClickListener(comentario -> {
            respondendoParaComentarioId = comentario.getId();
            editComentario.setHint("Respondendo a " + comentario.getNome());
        });

        carregarComentarios();

        buttonPublicar.setOnClickListener(v -> publicarComentario());

        return view;
    }

    private void carregarComentarios() {
        DatabaseReference comentariosRef = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animalId)
                .child("comentarios");

        comentariosRef.orderByChild("timestamp").limitToLast(comentariosVisiveis)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Comentarios> lista = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Comentarios c = snap.getValue(Comentarios.class);
                            if (c != null) lista.add(c);
                        }

                        // Ordem: mais recentes primeiro
                        Collections.reverse(lista);
                        listaComentarios.clear();
                        listaComentarios.addAll(lista);
                        adapter.notifyDataSetChanged();

                        // Contar total de comentários
                        comentariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<Comentarios> lista = new ArrayList<>();
                                long total = snapshot.getChildrenCount();

                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    Comentarios c = snap.getValue(Comentarios.class);
                                    if (c != null) lista.add(c);
                                }

                                Collections.reverse(lista);
                                listaComentarios.clear();
                                listaComentarios.addAll(lista.subList(0, Math.min(lista.size(), comentariosVisiveis)));
                                adapter.notifyDataSetChanged();

                                if (total > comentariosVisiveis) {
                                    verMaisComentarios.setVisibility(View.VISIBLE);
                                    verMaisComentarios.setText("Ver mais comentários (" + total + ")");
                                    verMaisComentarios.setOnClickListener(v -> {
                                        comentariosVisiveis += 10;
                                        carregarComentarios(); // recarrega com mais 10
                                    });
                                } else {
                                    verMaisComentarios.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Firebase", "Erro ao carregar comentários", error.toException());
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Erro ao carregar comentários", error.toException());
                    }
                });
    }

    private void publicarComentario() {
        String texto = editComentario.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) {
            Toast.makeText(getContext(), "Digite um comentário", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nome = snapshot.child("nickname").getValue(String.class);
                String foto = snapshot.child("photoUrl").getValue(String.class);

                Comentarios novoComentario = new Comentarios();
                novoComentario.setId(UUID.randomUUID().toString());
                novoComentario.setNome(nome != null ? nome : "Usuário");
                novoComentario.setTexto(texto);
                novoComentario.setTimestamp(System.currentTimeMillis());
                novoComentario.setFotoUrl(foto != null ? foto : "");
                novoComentario.setUserId(userId);

                if (respondendoParaComentarioId != null) {
                    FirebaseDatabase.getInstance()
                            .getReference("respostas")
                            .child(respondendoParaComentarioId)
                            .child(novoComentario.getId())
                            .setValue(novoComentario);
                } else {
                    FirebaseDatabase.getInstance()
                            .getReference("animais")
                            .child(animalId)
                            .child("comentarios")
                            .child(novoComentario.getId())
                            .setValue(novoComentario);
                }

                editComentario.setText("");
                editComentario.setHint("Adicione um comentário...");
                respondendoParaComentarioId = null;
                carregarComentarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
