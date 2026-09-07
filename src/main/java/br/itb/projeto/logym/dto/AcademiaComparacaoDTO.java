package br.itb.projeto.logym.dto;

import java.math.BigDecimal;
import java.util.List;

public record AcademiaComparacaoDTO(
        Long id,
        String nome,
        FotoPrincipalComparacaoDTO fotoPrincipal,
        BigDecimal nota,
        BigDecimal distanciaKm,
        String endereco,
        BigDecimal numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        List<ItemEstruturadoComparacaoDTO> categorias,
        List<ItemEstruturadoComparacaoDTO> facilidades,
        List<CriterioComparacaoDTO> criterios) {

    public record FotoPrincipalComparacaoDTO(Long id, String tipoArquivo, String url) {
    }

    public record ItemEstruturadoComparacaoDTO(Long id, String nome, String descricao) {
    }

    public record CriterioComparacaoDTO(Long id, String nome, BigDecimal media) {
    }
}
