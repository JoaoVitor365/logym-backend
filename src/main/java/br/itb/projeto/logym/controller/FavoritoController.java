package br.itb.projeto.logym.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.service.FavoritoService;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Boolean>> toggleFavorito(
            @RequestParam Long usuarioId,
            @RequestParam Long academiaId,
            Authentication authentication
    ) {
        boolean favoritado = favoritoService.toggleFavorito(usuarioId, academiaId, authentication);
        return ResponseEntity.ok(Map.of("favoritado", favoritado));
    }

    @GetMapping("/usuario/{usuarioId}/academia/{academiaId}")
    public ResponseEntity<Map<String, Boolean>> isFavorito(
            @PathVariable Long usuarioId,
            @PathVariable Long academiaId,
            Authentication authentication
    ) {
        boolean favoritado = favoritoService.isFavorito(usuarioId, academiaId, authentication);
        return ResponseEntity.ok(Map.of("favoritado", favoritado));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Academia>> findAcademiasFavoritas(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(favoritoService.findAcademiasFavoritas(usuarioId, authentication));
    }
}
