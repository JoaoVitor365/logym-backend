package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByStatusCategoriaOrderByNomeAsc(String statusCategoria);

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
