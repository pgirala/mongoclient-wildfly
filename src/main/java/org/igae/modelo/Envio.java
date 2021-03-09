package org.igae.modelo;

import java.util.Date;

public class Envio {
    private String idRemitente;
    private String idDestinatario;
    private String comentario;
    private Date momentoEnvio;

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

    @Override
    public String toString() {
        return "Envio{" +
                "idRemitente='" + idRemitente + '\'' +
                "idDestinatario='" + idDestinatario + '\'' +
                "comentario='" + comentario + '\'' +
                "momentoEnvio=" + (momentoEnvio == null ? "null":momentoEnvio.toString()) + '\'' +
                '}';
    }
}