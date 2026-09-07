package br.itb.projeto.logym.dto;

import java.math.BigDecimal;

import br.itb.projeto.logym.model.entity.Academia;

public record AcademiaProximaDTO(Academia academia, BigDecimal distanciaKm) {
}
