package com.company.miadot.model;

import java.io.Serializable;

public class Notificacao implements Serializable {
    private String id;
    private String tipo; // Ex: curtida, comentario, resposta, seguidor, mencao, compartilhamento, convite, lembrete, mensagem, interesse_adocao
    private String mensagem;
    private String remetenteId;
    private String destinatarioId;
    private String postId;
    private String petId;
    private String imagemUrl;
    private long timestamp;
    private boolean lida;

    public Notificacao() {}

    public Notificacao(String id, String tipo, String mensagem, String remetenteId, String destinatarioId, String postId, String petId, String imagemUrl, long timestamp, boolean lida) {
        this.id = id;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.remetenteId = remetenteId;
        this.destinatarioId = destinatarioId;
        this.postId = postId;
        this.petId = petId;
        this.imagemUrl = imagemUrl;
        this.timestamp = timestamp;
        this.lida = lida;
    }

    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getRemetenteId() { return remetenteId; }
    public void setRemetenteId(String remetenteId) { this.remetenteId = remetenteId; }
    public String getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(String destinatarioId) { this.destinatarioId = destinatarioId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}

