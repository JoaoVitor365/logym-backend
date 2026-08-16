package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.Facilidade;

public interface FacilidadeRepository extends JpaRepository<Facilidade, Long> {

    List<Facilidade> findByStatusFacilidadeOrderByNomeAsc(String statusFacilidade);

    Optional<Facilidade> findByNomeIgnoreCase(String nome);
}
