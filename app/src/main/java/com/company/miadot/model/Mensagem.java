package com.company.miadot.model;

import java.io.Serializable;

public class Mensagem implements Serializable {
    private String id;
    private String remetenteId;
    private String destinatarioId;
    private String texto;
    private String tipo; // texto, imagem, video, audio, sticker
    private String urlMidia;
    private long timestamp;
    private boolean visualizada;

    public Mensagem() {}

    public Mensagem(String id, String remetenteId, String destinatarioId, String texto, String tipo, String urlMidia, long timestamp, boolean visualizada) {
        this.id = id;
        this.remetenteId = remetenteId;
        this.destinatarioId = destinatarioId;
        this.texto = texto;
        this.tipo = tipo;
        this.urlMidia = urlMidia;
        this.timestamp = timestamp;
        this.visualizada = visualizada;
    }

    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRemetenteId() { return remetenteId; }
    public void setRemetenteId(String remetenteId) { this.remetenteId = remetenteId; }
    public String getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(String destinatarioId) { this.destinatarioId = destinatarioId; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getUrlMidia() { return urlMidia; }
    public void setUrlMidia(String urlMidia) { this.urlMidia = urlMidia; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isVisualizada() { return visualizada; }
    public void setVisualizada(boolean visualizada) { this.visualizada = visualizada; }
}

