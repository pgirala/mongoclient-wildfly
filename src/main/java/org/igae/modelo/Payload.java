package org.igae.modelo;

public class Payload {
    private Acreditacion data;

    public Acreditacion getData() {
        return data;
    }

    public void setData(Acreditacion acreditacion) {
        this.data = acreditacion;
    }

    @Override
    public String toString() {
        return "{\"data\":" + this.getData().toString() + "}";
    }
}
