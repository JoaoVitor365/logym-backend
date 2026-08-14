package br.itb.projeto.logym.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDTO {

    private Long id;
    private String comentario;
    private BigDecimal nota;
    private Long academiaId;
    private String academiaNome;
    private Long usuarioId;
    private String usuarioNome;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private String statusAvaliacao;
    private List<ItemNotaAvaliacaoDTO> itens = new ArrayList<>();

    public AvaliacaoDTO(
            Long id,
            String comentario,
            BigDecimal nota,
            Long academiaId,
            String academiaNome,
            Long usuarioId,
            String usuarioNome,
            LocalDateTime dataCadastro,
            LocalDateTime dataAtualizacao,
            String statusAvaliacao) {
        this.id = id;
        this.comentario = comentario;
        this.nota = nota;
        this.academiaId = academiaId;
        this.academiaNome = academiaNome;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = dataAtualizacao;
        this.statusAvaliacao = statusAvaliacao;
    }

    public AvaliacaoDTO(
            Long id,
            String comentario,
            BigDecimal nota,
            Long academiaId,
            String academiaNome,
            Long usuarioId,
            String usuarioNome,
            LocalDateTime dataCadastro,
            LocalDateTime dataAtualizacao,
            String statusAvaliacao,
            List<ItemNotaAvaliacaoDTO> itens) {
        this(id, comentario, nota, academiaId, academiaNome, usuarioId, usuarioNome, dataCadastro, dataAtualizacao, statusAvaliacao);
        this.itens = itens;
    }

    public Long getId() {
        return id;
    }

    public String getComentario() {
        return comentario;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public Long getAcademiaId() {
        return academiaId;
    }

    public String getAcademiaNome() {
        return academiaNome;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
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

    public List<ItemNotaAvaliacaoDTO> getItens() {
        return itens;
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

    public void setAcademiaId(Long academiaId) {
        this.academiaId = academiaId;
    }

    public void setAcademiaNome(String academiaNome) {
        this.academiaNome = academiaNome;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
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

    public void setItens(List<ItemNotaAvaliacaoDTO> itens) {
        this.itens = itens;
    }
}
