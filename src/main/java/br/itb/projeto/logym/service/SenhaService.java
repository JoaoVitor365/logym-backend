package br.itb.projeto.logym.service;

import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    public void validarSenha(String senha) {
        if (senha == null || senha.length() < 8 || senha.length() > 64
                || !senha.matches(".*[A-Z].*")
                || !senha.matches(".*[a-z].*")
                || !senha.matches(".*\\d.*")
                || !senha.matches(".*[^A-Za-z0-9].*")) {
            throw new RuntimeException(
                    "A senha deve ter entre 8 e 64 caracteres e conter letra maiuscula, letra minuscula, numero e caractere especial.");
        }
    }
}
