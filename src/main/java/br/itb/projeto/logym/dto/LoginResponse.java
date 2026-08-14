package br.itb.projeto.logym.dto;

public class LoginResponse {

    private String message;
    private UsuarioDTO usuario;

    public LoginResponse(String message, UsuarioDTO usuario) {
        this.message = message;
        this.usuario = usuario;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }
}
