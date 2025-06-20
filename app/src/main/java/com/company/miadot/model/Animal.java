package com.company.miadot.model;

import java.util.HashMap;
import java.util.Map;

public class Animal {
    private String id;
    private String nome;
    private String idade;
    private String estado;
    private String descricao;
    private String donoId;
    private String imageURL;
    private Integer likes;
    private int interessados;

    private String userId; // id de quem publicou

    private Map<String, Boolean> curtidas = new HashMap<>();

    public Map<String, Boolean> getCurtidas() {
        return curtidas;
    }

    public Animal() {
        // Construtor vazio para Firebase
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

    public String getIdade() {
        return idade;
    }

    public void setIdade(String idade) {
        this.idade = idade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDonoId() {
        return donoId;
    }

    public void setDonoId(String donoId) {
        this.donoId = donoId;
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
