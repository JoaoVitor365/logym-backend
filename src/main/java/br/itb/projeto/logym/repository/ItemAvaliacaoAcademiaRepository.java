package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.itb.projeto.logym.model.entity.ItemAvaliacaoAcademia;

public interface ItemAvaliacaoAcademiaRepository extends JpaRepository<ItemAvaliacaoAcademia, Long> {

    @Query("""
        SELECT iaa
        FROM ItemAvaliacaoAcademia iaa
        WHERE iaa.academia.id = :academiaId
        AND iaa.usuario.id = :usuarioId
        ORDER BY iaa.item.id ASC
    """)
    List<ItemAvaliacaoAcademia> findByAcademiaIdAndUsuarioIdOrderByItemIdAsc(Long academiaId, Long usuarioId);

    @Query("""
        SELECT iaa
        FROM ItemAvaliacaoAcademia iaa
        WHERE iaa.academia.id = :academiaId
        AND iaa.usuario.id = :usuarioId
        AND iaa.item.id = :itemId
    """)
    Optional<ItemAvaliacaoAcademia> findByAcademiaIdAndUsuarioIdAndItemId(
            Long academiaId,
            Long usuarioId,
            Long itemId
    );

    @Query("""
        SELECT iaa.academia.id, iaa.item.id, AVG(iaa.nota)
        FROM ItemAvaliacaoAcademia iaa
        JOIN Avaliacao a ON a.academia.id = iaa.academia.id
            AND a.usuario.id = iaa.usuario.id
        WHERE iaa.academia.id IN :academiaIds
            AND iaa.statusAvaliacao = true
            AND a.statusAvaliacao = 'ATIVO'
        GROUP BY iaa.academia.id, iaa.item.id
    """)
    List<Object[]> calcularMediasAtivasPorAcademiaIds(@Param("academiaIds") List<Long> academiaIds);
}
