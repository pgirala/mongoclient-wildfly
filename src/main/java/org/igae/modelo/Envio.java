package org.igae.modelo;

public class Envio {
    private String comentario;

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    @Override
    public String toString() {
        return "Envio{" +
                "comentario='" + comentario + '\'' +
                '}';
    }
}