package br.itb.projeto.logym.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.Gerente;

public interface GerenteRepository extends JpaRepository<Gerente, Long> {

    Optional<Gerente> findByUsuarioId(Long usuarioId);

    boolean existsByCpf(String cpf);
}