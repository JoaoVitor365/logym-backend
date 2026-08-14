package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
