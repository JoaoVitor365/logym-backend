package br.itb.projeto.logym.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "FotoAcademia")
public class FotoAcademia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private byte[] foto;

    @Column(length = 100, nullable = false)
    private String tipoArquivo;

    @ManyToOne
    @JoinColumn(name = "academia_id", nullable = false)
    private Academia academia;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    @Column(length = 20, nullable = false)
    private String statusFoto;

    public Long getId() {
        return id;
    }

    public byte[] getFoto() {
        return foto;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public Academia getAcademia() {
        return academia;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public String getStatusFoto() {
        return statusFoto;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
    }

    public void setAcademia(Academia academia) {
        this.academia = academia;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public void setStatusFoto(String statusFoto) {
        this.statusFoto = statusFoto;
    }
}