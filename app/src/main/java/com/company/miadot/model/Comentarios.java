package com.company.miadot.model;

import java.util.HashMap;
import java.util.Map;

public class Comentarios {
    private String id;
    private String nome;
    private String texto;
    private String fotoUrl;
    private long timestamp;
    private String parentId; // null se for comentário principal
    private String respostaPara;
    private String userId;
    public Comentarios() {}

    public Comentarios(String id, String nome, String texto, String fotoUrl, long timestamp, String parentId) {
        this.id = id;
        this.nome = nome;
        this.texto = texto;
        this.fotoUrl = fotoUrl;
        this.timestamp = timestamp;
        this.parentId = parentId;
    }

    // Getters e Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("nome", nome);
        map.put("texto", texto);
        map.put("fotoUrl", fotoUrl);
        map.put("timestamp", timestamp);
        map.put("parentId", parentId);
        return map;
    }
    public String getRespostaPara() {
        return respostaPara;
    }

    public void setRespostaPara(String respostaPara) {
        this.respostaPara = respostaPara;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
