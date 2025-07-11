package com.company.miadot.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.miadot.R;
import com.company.miadot.activities.AnimalDetailActivity;
import com.company.miadot.model.Animal;

import java.util.List;

public class ProfileAnimalAdapter extends RecyclerView.Adapter<ProfileAnimalAdapter.AnimalViewHolder> {

    private Context context;
    private List<Animal> animalList;

    public ProfileAnimalAdapter(Context context, List<Animal> animalList) {
        this.context = context;
        this.animalList = animalList;
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
        Glide.with(context).load(animal.getImageURL()).into(holder.imageAnimal);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimalDetailActivity.class);
            intent.putExtra("animalId", animal.getId());
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimalDetailActivity.class);
            intent.putExtra("imageUrl", animal.getImageURL());
            intent.putExtra("nome", animal.getNome());
            intent.putExtra("descricao", animal.getDescricao());
            intent.putExtra("idade", animal.getIdade());
            intent.putExtra("estado", animal.getEstado());
            context.startActivity(intent);
        });
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
