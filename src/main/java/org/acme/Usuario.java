package org.acme;




public class Usuario {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "email='" + email + '\'' +
                '}';
    }
}