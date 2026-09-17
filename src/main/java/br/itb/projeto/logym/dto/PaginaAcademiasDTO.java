package br.itb.projeto.logym.dto;

import java.util.List;

import br.itb.projeto.logym.model.entity.Academia;

public record PaginaAcademiasDTO(
        List<Academia> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
