package br.itb.projeto.logym.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.logym.dto.AvaliacaoDTO;
import br.itb.projeto.logym.dto.AvaliacaoPorItensRequestDTO;
import br.itb.projeto.logym.dto.ItemAvaliacaoDTO;
import br.itb.projeto.logym.service.AvaliacaoService;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/itens")
    public ResponseEntity<List<ItemAvaliacaoDTO>> listarItensAvaliacao() {
        return ResponseEntity.ok(avaliacaoService.listarItensAvaliacao());
    }

    @GetMapping("/academia/{academiaId}")
    public ResponseEntity<List<AvaliacaoDTO>> findByAcademiaId(
            @PathVariable Long academiaId,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(avaliacaoService.findByAcademiaId(academiaId, usuarioId));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AvaliacaoDTO>> findAllAdmin() {
        return ResponseEntity.ok(avaliacaoService.findAllAdmin());
    }

    @PostMapping
    public ResponseEntity<AvaliacaoDTO> avaliar(
            @RequestParam Long usuarioId,
            @RequestParam Long academiaId,
            @RequestBody AvaliacaoPorItensRequestDTO avaliacao
    ) {
        return ResponseEntity.ok(avaliacaoService.avaliarPorItens(usuarioId, academiaId, avaliacao));
    }

    @PutMapping("/{avaliacaoId}/inativar")
    public ResponseEntity<Void> inativar(
            @PathVariable Long avaliacaoId,
            @RequestParam Long usuarioId
    ) {
        avaliacaoService.inativar(avaliacaoId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/{avaliacaoId}/suspender")
    public ResponseEntity<AvaliacaoDTO> suspenderAdmin(@PathVariable Long avaliacaoId) {
        return ResponseEntity.ok(avaliacaoService.suspenderAdmin(avaliacaoId));
    }

    @PutMapping("/admin/{avaliacaoId}/reativar")
    public ResponseEntity<AvaliacaoDTO> reativarAdmin(@PathVariable Long avaliacaoId) {
        return ResponseEntity.ok(avaliacaoService.reativarAdmin(avaliacaoId));
    }
}
