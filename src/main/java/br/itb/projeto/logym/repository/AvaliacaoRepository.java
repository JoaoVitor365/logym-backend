package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.itb.projeto.logym.model.entity.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByAcademiaIdAndStatusAvaliacaoOrderByDataCadastroDesc(
            Long academiaId,
            String statusAvaliacao
    );

    List<Avaliacao> findAllByOrderByDataCadastroDesc();

    Optional<Avaliacao> findByUsuarioIdAndAcademiaId(Long usuarioId, Long academiaId);

    @Query("""
        SELECT AVG(a.nota)
        FROM Avaliacao a
        WHERE a.academia.id = :academiaId
        AND a.statusAvaliacao = 'ATIVO'
    """)
    Double calcularMediaPorAcademiaId(Long academiaId);
}
