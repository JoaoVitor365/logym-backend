package br.itb.projeto.logym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.FacilidadeAcademia;

public interface FacilidadeAcademiaRepository extends JpaRepository<FacilidadeAcademia, Long> {

    List<FacilidadeAcademia> findByAcademiaId(Long academiaId);

    List<FacilidadeAcademia> findByFacilidadeId(Long facilidadeId);

    List<FacilidadeAcademia> findByAcademiaIdAndStatusFacilidadeAcademia(Long academiaId, String statusFacilidadeAcademia);
}
