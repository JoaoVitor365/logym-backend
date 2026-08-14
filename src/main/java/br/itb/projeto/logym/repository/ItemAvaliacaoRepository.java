package br.itb.projeto.logym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.ItemAvaliacao;

public interface ItemAvaliacaoRepository extends JpaRepository<ItemAvaliacao, Long> {

    List<ItemAvaliacao> findAllByOrderByIdAsc();
}
