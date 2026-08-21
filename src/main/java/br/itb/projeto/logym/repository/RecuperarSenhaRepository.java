package br.itb.projeto.logym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.itb.projeto.logym.model.entity.RecuperarSenha;

public interface RecuperarSenhaRepository extends JpaRepository<RecuperarSenha, Integer> {

    List<RecuperarSenha> findByEmailAndStatusCodigoTrue(String email);

    Optional<RecuperarSenha> findFirstByEmailAndCodigoAndStatusCodigoTrueOrderByGeradoEmDesc(String email, String codigo);
}
