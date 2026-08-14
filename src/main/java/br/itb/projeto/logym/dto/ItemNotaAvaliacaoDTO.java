package br.itb.projeto.logym.dto;

import java.math.BigDecimal;

public class ItemNotaAvaliacaoDTO {

    private Long itemId;
    private String itemNome;
    private String itemDescricao;
    private BigDecimal nota;
    private Boolean statusAvaliacao;

    public ItemNotaAvaliacaoDTO() {
    }

    public ItemNotaAvaliacaoDTO(
            Long itemId,
            String itemNome,
            String itemDescricao,
            BigDecimal nota,
            Boolean statusAvaliacao) {
        this.itemId = itemId;
        this.itemNome = itemNome;
        this.itemDescricao = itemDescricao;
        this.nota = nota;
        this.statusAvaliacao = statusAvaliacao;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemNome() {
        return itemNome;
    }

    public void setItemNome(String itemNome) {
        this.itemNome = itemNome;
    }

    public String getItemDescricao() {
        return itemDescricao;
    }

    public void setItemDescricao(String itemDescricao) {
        this.itemDescricao = itemDescricao;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public Boolean getStatusAvaliacao() {
        return statusAvaliacao;
    }

    public void setStatusAvaliacao(Boolean statusAvaliacao) {
        this.statusAvaliacao = statusAvaliacao;
    }
}
