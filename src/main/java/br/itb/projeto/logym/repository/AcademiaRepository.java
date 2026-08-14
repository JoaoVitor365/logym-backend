package br.itb.projeto.logym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.Academia;

public interface AcademiaRepository extends JpaRepository<Academia, Long> {

    List<Academia> findByStatusAcademia(String statusAcademia);

    List<Academia> findByGerenteIdAndStatusAcademia(Long gerenteId, String statusAcademia);

    List<Academia> findByGerenteId(Long gerenteId);
}