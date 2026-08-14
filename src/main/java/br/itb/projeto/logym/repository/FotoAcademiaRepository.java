package br.itb.projeto.logym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.FotoAcademia;

public interface FotoAcademiaRepository extends JpaRepository<FotoAcademia, Long> {

    List<FotoAcademia> findByAcademiaIdAndStatusFotoOrderByDataCadastroDesc(
            Long academiaId,
            String statusFoto
    );
}