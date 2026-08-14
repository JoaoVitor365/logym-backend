package br.itb.projeto.logym.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "Avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String comentario;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal nota;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    private LocalDateTime dataAtualizacao;

    @Column(length = 20, nullable = false)
    private String statusAvaliacao;

    public Long getId() {
        return id;
    }

    public String getComentario() {
        return comentario;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public Academia getAcademia() {
        return academia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public String getStatusAvaliacao() {
        return statusAvaliacao;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public void setStatusAvaliacao(String statusAvaliacao) {
        this.statusAvaliacao = statusAvaliacao;
    }
}