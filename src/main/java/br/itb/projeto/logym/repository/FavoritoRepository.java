package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Favorito;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    Optional<Favorito> findByUsuarioIdAndAcademiaId(Long usuarioId, Long academiaId);

    @Query("""
        SELECT f.academia
        FROM Favorito f
        WHERE f.usuario.id = :usuarioId
        AND f.statusFavorito = true
        AND f.academia.statusAcademia = 'ATIVO'
    """)
    List<Academia> findAcademiasFavoritasAtivasByUsuarioId(@Param("usuarioId") Long usuarioId);
}