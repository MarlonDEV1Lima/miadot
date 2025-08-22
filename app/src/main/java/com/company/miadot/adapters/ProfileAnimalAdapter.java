package com.company.miadot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.activities.OnAnimalClickListener;
import com.company.miadot.model.Animal;

import java.util.List;
import android.util.Log;

public class ProfileAnimalAdapter extends RecyclerView.Adapter<ProfileAnimalAdapter.AnimalViewHolder> {

    private Context context;
    private List<Animal> animalList;
    private OnAnimalClickListener listener; // Nova variável para armazenar o listener

    // Modifique o construtor para aceitar o OnAnimalClickListener
    public ProfileAnimalAdapter(Context context, List<Animal> animalList, OnAnimalClickListener listener) {
        this.context = context;
        this.animalList = animalList;
        this.listener = listener; // Atribui o listener passado
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_animal_profile, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animalList.get(position);

        // Carrega a imagem do animal. Assumindo que getImageURL() é o método correto
        // para obter a URL da imagem no seu objeto Animal.
        if (animal.getImageURL() != null && !animal.getImageURL().isEmpty()) {
            Glide.with(context)
                    .load(animal.getImageURL())
                    .into(holder.imageAnimal);
        } else {
            // Define uma imagem de placeholder se a URL for nula ou vazia
            holder.imageAnimal.setImageResource(R.drawable.default_profile);
        }

        // Define o OnClickListener para o item.
        // Ao invés de iniciar uma nova Activity, chamamos o método no listener
        // (que é a ProfileActivity), que por sua vez exibirá o DialogFragment.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                android.util.Log.d("ProfileAdapter", "Item de animal clicado: " + animal.getNome()); // Adicione esta linha
                listener.onAnimalClick(animal);
            }
        });

        // Removi o segundo holder.itemView.setOnClickListener() que estava duplicado
        // e sobrescrevia o primeiro, além de iniciar a AnimalDetailActivity.
    }

    @Override
    public int getItemCount() {
        return animalList.size();
    }

    public static class AnimalViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAnimal;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAnimal = itemView.findViewById(R.id.imageAnimalProfile);
        }
    }
}
