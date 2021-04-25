package org.igae.modelo;

import java.util.Date;

public class Envio {
    private String id;
    private String idRemitente;
    private String idDestinatario;
    private String comentario;
    private Date momentoEnvio;
    private String dominio;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdRemitente() {
        return idRemitente;
    }

    public void setIdRemitente(String idRemitente) {
        this.idRemitente = idRemitente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getMomentoEnvio() {
        return momentoEnvio;
    }

    public void setMomentoEnvio(Date momentoEnvio) {
        this.momentoEnvio = momentoEnvio;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    @Override
    public String toString() {
        return "Envio{" + (id == null ? "null" : id) + ", idRemitente='" + idRemitente + '\'' + ", idDestinatario='"
                + idDestinatario + '\'' + ", comentario='" + (comentario == null ? "null" : comentario) + '\''
                + ", momentoEnvio=" + (momentoEnvio == null ? "null" : momentoEnvio.toString()) + '\'' + ", dominio="
                + dominio + '}';
    }
}