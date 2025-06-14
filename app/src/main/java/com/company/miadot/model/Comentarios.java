package com.company.miadot.model;

public class Comentarios {
    private String id;
    private String nome;
    private String texto;
    private long timestamp;

    public Comentarios() {
        // construtor vazio necessário para Firebase
    }

//    public Comentarios(String nome, String texto, long timestamp) {
//        this.nome = nome;
//        this.texto = texto;
//        this.timestamp = timestamp;
//    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
