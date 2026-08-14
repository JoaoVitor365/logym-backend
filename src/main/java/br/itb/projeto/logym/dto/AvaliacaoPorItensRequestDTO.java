package br.itb.projeto.logym.dto;

import java.util.List;

public class AvaliacaoPorItensRequestDTO {

    private List<ItemNotaAvaliacaoDTO> itens;

    public List<ItemNotaAvaliacaoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemNotaAvaliacaoDTO> itens) {
        this.itens = itens;
    }
}
