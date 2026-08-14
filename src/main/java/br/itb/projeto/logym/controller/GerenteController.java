package br.itb.projeto.logym.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.itb.projeto.logym.model.entity.Gerente;
import br.itb.projeto.logym.service.GerenteService;

@RestController
@RequestMapping("/gerentes")
public class GerenteController {

    private final GerenteService gerenteService;

    public GerenteController(GerenteService gerenteService) {
        this.gerenteService = gerenteService;
    }

    @PostMapping({ "", "/" })
    public ResponseEntity<Gerente> create(@RequestBody Gerente gerente) {
        Gerente novoGerente = gerenteService.create(gerente);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoGerente);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Gerente>> findAll() {
        return ResponseEntity.ok(gerenteService.findAll());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Gerente> findByUsuarioId(@PathVariable Long usuarioId) {
        return gerenteService.findByUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gerente> findById(@PathVariable Long id) {
        return ResponseEntity.ok(gerenteService.findById(id));
    }
}