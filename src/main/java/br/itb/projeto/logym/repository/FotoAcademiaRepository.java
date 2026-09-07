package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.itb.projeto.logym.model.entity.FotoAcademia;

public interface FotoAcademiaRepository extends JpaRepository<FotoAcademia, Long> {

    List<FotoAcademia> findByAcademiaIdAndStatusFotoOrderByDataCadastroDesc(
            Long academiaId,
            String statusFoto
    );

    Optional<FotoAcademia> findByAcademiaIdAndStatusFotoAndPrincipalTrue(
            Long academiaId,
            String statusFoto
    );

    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE FotoAcademia foto
        SET foto.principal = false
        WHERE foto.academia.id = :academiaId
        AND foto.principal = true
    """)
    int desmarcarPrincipalDaAcademia(@Param("academiaId") Long academiaId);
}
