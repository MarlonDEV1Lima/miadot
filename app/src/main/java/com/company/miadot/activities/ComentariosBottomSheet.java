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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.miadot.R;
import com.company.miadot.adapters.ComentarioAdapter;
import com.company.miadot.model.Comentarios;
import com.company.miadot.model.Notificacao;
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
    private Set<String> comentarioIds = new HashSet<>();
    private String respondendoParaComentarioId = null;

    private int comentariosVisiveis = 10;
    private TextView verMaisComentarios;

    private DatabaseReference comentariosRef;
    private ChildEventListener comentariosListener;

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
        comentariosRef = FirebaseDatabase.getInstance()
                .getReference("animais")
                .child(animalId)
                .child("comentarios");

        if (comentariosListener != null) {
            comentariosRef.removeEventListener(comentariosListener);
        }

        listaComentarios.clear();
        comentarioIds.clear();
        adapter.notifyDataSetChanged();

        comentariosListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Comentarios comentario = snapshot.getValue(Comentarios.class);
                if (comentario != null && comentarioIds.add(comentario.getId())) {
                    listaComentarios.add(0, comentario); // Mais recente no topo
                    adapter.notifyItemInserted(0);
                    recyclerComentarios.scrollToPosition(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Comentarios atualizado = snapshot.getValue(Comentarios.class);
                if (atualizado != null) {
                    for (int i = 0; i < listaComentarios.size(); i++) {
                        if (listaComentarios.get(i).getId().equals(atualizado.getId())) {
                            listaComentarios.set(i, atualizado);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Comentarios removido = snapshot.getValue(Comentarios.class);
                if (removido != null) {
                    for (int i = 0; i < listaComentarios.size(); i++) {
                        if (listaComentarios.get(i).getId().equals(removido.getId())) {
                            listaComentarios.remove(i);
                            comentarioIds.remove(removido.getId());
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        comentariosRef.orderByChild("timestamp").limitToLast(comentariosVisiveis)
                .addChildEventListener(comentariosListener);

        verMaisComentarios.setText(getString(R.string.ver_mais_comentarios));
        verMaisComentarios.setVisibility(View.VISIBLE);
        verMaisComentarios.setOnClickListener(v -> {
            comentariosVisiveis += 10;
            carregarComentarios(); // recarrega com mais 10 visíveis
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (comentariosRef != null && comentariosListener != null) {
            comentariosRef.removeEventListener(comentariosListener);
        }

        if (adapter != null) {
            adapter.removerListeners();
        }
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
                String comentarioId = UUID.randomUUID().toString();
                novoComentario.setId(comentarioId);
                novoComentario.setNome(nome != null ? nome : "Usuário");
                novoComentario.setTexto(texto);
                novoComentario.setTimestamp(System.currentTimeMillis());
                novoComentario.setFotoUrl(foto != null ? foto : "");
                novoComentario.setUserId(userId);

                DatabaseReference comentariosRef;
                if (respondendoParaComentarioId != null) {
                    // É uma resposta
                    comentariosRef = FirebaseDatabase.getInstance()
                            .getReference("respostas")
                            .child(respondendoParaComentarioId)
                            .child(comentarioId);
                } else {
                    // É um comentário novo
                    comentariosRef = FirebaseDatabase.getInstance()
                            .getReference("animais")
                            .child(animalId)
                            .child("comentarios")
                            .child(comentarioId);
                }

                comentariosRef.setValue(novoComentario).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        editComentario.setText("");
                        editComentario.setHint("Adicione um comentário...");
                        respondendoParaComentarioId = null;

                        // Buscar o dono do animal para enviar notificação
                        DatabaseReference animalRef = FirebaseDatabase.getInstance()
                                .getReference("animais")
                                .child(animalId);

                        animalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot animalSnapshot) {
                                String donoId = animalSnapshot.child("donoId").getValue(String.class);

                                if (donoId != null && !donoId.equals(userId)) {
                                    // Criar notificação para o dono
                                    String notificacaoId = UUID.randomUUID().toString();
                                    long timestamp = System.currentTimeMillis();
                                    Notificacao notificacao = new Notificacao(
                                            notificacaoId, // id
                                            "comentario", // tipo
                                            nome + " comentou no seu post", // mensagem
                                            userId, // remetenteId
                                            donoId, // destinatarioId
                                            null, // postId
                                            null, // petId
                                            null, // imagemUrl
                                            timestamp, // timestamp
                                            false // lida
                                    );

                                    FirebaseDatabase.getInstance()
                                            .getReference("notificacoes")
                                            .child(donoId)
                                            .child(notificacaoId)
                                            .setValue(notificacao);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}
