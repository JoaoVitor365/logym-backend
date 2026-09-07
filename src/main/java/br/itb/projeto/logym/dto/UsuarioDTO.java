package br.itb.projeto.logym.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UsuarioDTO {

    private Long id;
    private String nome;
    private String username;
    private String nivelAcesso;
    private String cep;
    private BigDecimal numero;
    private String complemento;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private byte[] foto;
    private String statusUsuario;
    private LocalDateTime dataCadastro;

    public UsuarioDTO(
            Long id,
            String nome,
            String username,
            String nivelAcesso,
            String cep,
            BigDecimal numero,
            String complemento,
            BigDecimal latitude,
            BigDecimal longitude,
            byte[] foto,
            LocalDateTime dataCadastro,
            String statusUsuario
    ) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.nivelAcesso = nivelAcesso;
        this.cep = cep;
        this.numero = numero;
        this.complemento = complemento;
        this.latitude = latitude;
        this.longitude = longitude;
        this.foto = foto;
        this.statusUsuario = statusUsuario;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsername() {
        return username;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public String getCep() {
        return cep;
    }

    public BigDecimal getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public byte[] getFoto() {
        return foto;
    }

    public String getStatusUsuario() {
        return statusUsuario;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setNumero(BigDecimal numero) {
        this.numero = numero;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public void setStatusUsuario(String statusUsuario) {
        this.statusUsuario = statusUsuario;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
