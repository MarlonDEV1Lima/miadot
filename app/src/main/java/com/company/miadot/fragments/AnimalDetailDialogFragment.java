// AnimalDetailDialogFragment.java
package com.company.miadot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.ColorDrawable; // Necessário para definir um fundo transparente ao diálogo
import android.util.DisplayMetrics; // Necessário para obter as dimensões da tela

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.model.Animal;

public class AnimalDetailDialogFragment extends DialogFragment {

    private static final String ARG_ANIMAL = "animal";

    public static AnimalDetailDialogFragment newInstance(Animal animal) {
        AnimalDetailDialogFragment fragment = new AnimalDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ANIMAL, animal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o estilo do diálogo como flutuante
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FloatingAnimalDetailDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout activity_animal_detail.xml para ser o conteúdo do pop-up
        return inflater.inflate(R.layout.activity_animal_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recupera o objeto Animal passado como argumento
        Animal animal = null;
        if (getArguments() != null) {
            animal = (Animal) getArguments().getSerializable(ARG_ANIMAL);
        }

        // Se o objeto Animal for nulo, fecha o diálogo
        if (animal == null) {
            dismiss();
            return;
        }

        // Inicializa as views do layout
        ImageView imageAnimalDetail = view.findViewById(R.id.imageAnimalDetail);
        TextView textNomeAnimal = view.findViewById(R.id.textNomeAnimal);
        TextView textDescricao = view.findViewById(R.id.textDescricao);
        TextView textIdade = view.findViewById(R.id.textIdade);
        TextView textEstado = view.findViewById(R.id.textEstado);
        ImageView closeButton = view.findViewById(R.id.closeButton); // O botão de fechar

        // Preenche as views com os dados do animal
        if (animal.getImageURL() != null && !animal.getImageURL().isEmpty()) {
            Glide.with(this)
                    .load(animal.getImageURL())
                    .into(imageAnimalDetail);
        } else {
            imageAnimalDetail.setImageResource(R.drawable.default_profile); // Usando default_profile como placeholder
        }

        textNomeAnimal.setText(animal.getNome() != null ? animal.getNome() : "Nome não disponível");
        textDescricao.setText(animal.getDescricao() != null ? animal.getDescricao() : "Sem descrição.");
        textIdade.setText(String.format("Idade: %s", animal.getIdade() != null ? animal.getIdade() : "Não informada"));
        textEstado.setText(String.format("Estado: %s", animal.getEstado() != null ? animal.getEstado() : "Não informado"));

        // Configura o listener para o botão de fechar (se existir no layout)
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Define a largura do diálogo como uma porcentagem da largura da tela
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.90); // 90% da largura da tela
            int height = ViewGroup.LayoutParams.WRAP_CONTENT; // Ajusta a altura ao conteúdo

            getDialog().getWindow().setLayout(width, height);

            // Opcional: Para garantir que não haja um fundo padrão indesejado que possa interferir no padding
            // ou na transparência, você pode definir o background do window como transparente.
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }
}
