package br.itb.projeto.logym.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.logym.dto.RedefinirSenhaDTO;
import br.itb.projeto.logym.dto.SolicitarCodigoDTO;
import br.itb.projeto.logym.dto.ValidarCodigoDTO;
import br.itb.projeto.logym.service.EmailEnvioException;
import br.itb.projeto.logym.service.RecuperarSenhaService;

@RestController
@RequestMapping("/recuperar-senha")
public class RecuperarSenhaController {

    private final RecuperarSenhaService recuperarSenhaService;

    public RecuperarSenhaController(RecuperarSenhaService recuperarSenhaService) {
        this.recuperarSenhaService = recuperarSenhaService;
    }

    @PostMapping("/solicitar-codigo")
    public ResponseEntity<Map<String, Object>> solicitarCodigo(@RequestBody SolicitarCodigoDTO dados) {
        try {
            String mensagem = recuperarSenhaService.solicitarCodigo(dados.getEmail());
            return ResponseEntity.ok(Map.of("message", mensagem));
        } catch (EmailEnvioException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Nao foi possivel enviar o codigo de recuperacao. Tente novamente mais tarde."));
        }
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<Map<String, Object>> validarCodigo(@RequestBody ValidarCodigoDTO dados) {
        try {
            recuperarSenhaService.validarCodigo(dados.getEmail(), dados.getCodigo());
            return ResponseEntity.ok(Map.of("valido", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valido", false, "message", "Codigo invalido ou expirado."));
        }
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody RedefinirSenhaDTO dados) {
        try {
            recuperarSenhaService.redefinirSenha(dados.getEmail(), dados.getCodigo(), dados.getNovaSenha());
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
