package org.igae.modelo;

public class Acreditacion {
    private String email;
    private String password;

    public Acreditacion() {

    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public String toString() {
        return "{\"email\":\"" + this.getEmail() + "\", \"password\":\"" + this.getPassword() + "\"}";
    }
}
