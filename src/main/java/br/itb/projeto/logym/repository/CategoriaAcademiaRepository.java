package br.itb.projeto.logym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.CategoriaAcademia;

public interface CategoriaAcademiaRepository extends JpaRepository<CategoriaAcademia, Long> {

    List<CategoriaAcademia> findByAcademiaId(Long academiaId);

    List<CategoriaAcademia> findByAcademiaIdAndCategoriaId(Long academiaId, Long categoriaId);

    List<CategoriaAcademia> findByAcademiaIdAndStatusCategoriaAcademia(Long academiaId, String statusCategoriaAcademia);
}
