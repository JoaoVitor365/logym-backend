package br.itb.projeto.logym.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GeocodificacaoException extends ResponseStatusException {

    private static final String MENSAGEM_SEGURA =
            "Nao foi possivel localizar o endereco informado. Verifique o CEP, numero e endereco.";

    public GeocodificacaoException(String message) {
        super(HttpStatus.BAD_REQUEST, MENSAGEM_SEGURA);
    }

    public GeocodificacaoException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, MENSAGEM_SEGURA, cause);
    }
}
