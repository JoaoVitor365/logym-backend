package br.itb.projeto.logym.dto;

public class RedefinirSenhaDTO {

    private String email;
    private String codigo;
    private String novaSenha;

    public String getEmail() { return email; }
    public String getCodigo() { return codigo; }
    public String getNovaSenha() { return novaSenha; }
    public void setEmail(String email) { this.email = email; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
