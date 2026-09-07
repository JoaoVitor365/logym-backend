package br.itb.projeto.logym.dto;

import java.math.BigDecimal;

public record EnderecoGeocodificacaoDTO(
        String endereco,
        BigDecimal numero,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String complemento) {
}
