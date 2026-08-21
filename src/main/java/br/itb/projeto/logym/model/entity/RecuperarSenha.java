package br.itb.projeto.logym.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RecuperarSenha")
public class RecuperarSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime geradoEm;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private Boolean statusCodigo;

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getGeradoEm() { return geradoEm; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public Boolean getStatusCodigo() { return statusCodigo; }
    public void setId(Integer id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setGeradoEm(LocalDateTime geradoEm) { this.geradoEm = geradoEm; }
    public void setExpiraEm(LocalDateTime expiraEm) { this.expiraEm = expiraEm; }
    public void setStatusCodigo(Boolean statusCodigo) { this.statusCodigo = statusCodigo; }
}
