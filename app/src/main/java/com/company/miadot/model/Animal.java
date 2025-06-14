package com.company.miadot.model;

import java.util.HashMap;
import java.util.Map;

public class Animal {
    private String id;
    private String nome;
    private String imageURL;
    private Integer likes;
    private int interessados;
    private Map<String, Boolean> curtidas = new HashMap<>();

    public Map<String, Boolean> getCurtidas() {
        return curtidas;
    }

    public Animal() {
        // Construtor vazio para Firebase
    }



    public Animal(String id, String nome, String imageURL, Integer likes, int interessados) {
        this.id = id;
        this.nome = nome;
        this.imageURL = imageURL;
        this.likes = likes;
        this.interessados = interessados;
    }

    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public int getInteressados() { return interessados; }
    public void setInteressados(int interessados) { this.interessados = interessados; }

    public void setCurtidas(Map<String, Boolean> curtidas) {
        this.curtidas = curtidas;
    }
}
