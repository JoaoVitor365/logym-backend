package br.itb.projeto.logym.dto;

import java.time.LocalDateTime;

public class FotoAcademiaDTO {

    private Long id;
    private Long academiaId;
    private String tipoArquivo;
    private LocalDateTime dataCadastro;

    public FotoAcademiaDTO(Long id, Long academiaId, String tipoArquivo, LocalDateTime dataCadastro) {
        this.id = id;
        this.academiaId = academiaId;
        this.tipoArquivo = tipoArquivo;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() {
        return id;
    }

    public Long getAcademiaId() {
        return academiaId;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}