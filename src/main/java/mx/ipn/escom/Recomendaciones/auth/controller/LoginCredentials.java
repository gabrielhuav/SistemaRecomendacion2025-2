package mx.ipn.escom.Recomendaciones.auth.controller;


// Clase LoginCredentials, debe coincidir con el modelo en tu app Android
public class LoginCredentials {
    private String correo;
    private String password;

    // Getters y Setters
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
