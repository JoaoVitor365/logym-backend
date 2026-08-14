package br.itb.projeto.logym.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "Favorito")
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    @Column(nullable = false)
    private Boolean statusFavorito;

    public Long getId() {
        return id;
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

    public Boolean getStatusFavorito() {
        return statusFavorito;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setStatusFavorito(Boolean statusFavorito) {
        this.statusFavorito = statusFavorito;
    }
}