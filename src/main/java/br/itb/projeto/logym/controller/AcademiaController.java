package br.itb.projeto.logym.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.service.AcademiaService;

@RestController
@RequestMapping("/academias")
public class AcademiaController {

    private final AcademiaService academiaService;

    public AcademiaController(AcademiaService academiaService) {
        this.academiaService = academiaService;
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<List<Academia>> findAll() {
        return ResponseEntity.ok(academiaService.findAllAtivas());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Academia>> findAllAdmin() {
        return ResponseEntity.ok(academiaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Academia> findById(@PathVariable Long id) {
        return ResponseEntity.ok(academiaService.findById(id));
    }

    @GetMapping("/gerente/{gerenteId}")
    public ResponseEntity<List<Academia>> findByGerenteId(@PathVariable Long gerenteId) {
        return ResponseEntity.ok(academiaService.findByGerenteId(gerenteId));
    }

    @GetMapping("/proximas/usuario/{usuarioId}")
    public ResponseEntity<List<Academia>> findProximasPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(academiaService.findProximasPorUsuario(usuarioId));
    }

    @PostMapping({ "", "/" })
    public ResponseEntity<Academia> create(@RequestBody Academia academia) {
        Academia novaAcademia = academiaService.create(academia);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaAcademia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Academia> update(
            @PathVariable Long id,
            @RequestBody Academia academia) {
        return ResponseEntity.ok(academiaService.update(id, academia));
    }

    /**
     * Fluxo do GERENTE: inativa a própria academia.
     */
    @PutMapping("/{id}/inativar")
    public ResponseEntity<Academia> inativar(@PathVariable Long id) {
        return ResponseEntity.ok(academiaService.inativar(id));
    }

    /**
     * Fluxo do GERENTE: reativa apenas academia INATIVA.
     * Se estiver SUSPENSA, o service bloqueia.
     */
    @PutMapping("/{id}/reativar")
    public ResponseEntity<Academia> reativar(@PathVariable Long id) {
        Academia academia = academiaService.reativar(id);
        return ResponseEntity.ok(academia);
    }

    /**
     * Fluxo do ADMIN: suspende academia no banco com status SUSPENSA.
     */
    @PutMapping("/admin/{id}/suspender")
    public ResponseEntity<Academia> suspenderAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(academiaService.suspenderAdmin(id));
    }

    /**
     * Fluxo do ADMIN: reativa academia INATIVA ou SUSPENSA.
     */
    @PutMapping("/admin/{id}/reativar")
    public ResponseEntity<Academia> reativarAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(academiaService.reativarAdmin(id));
    }
}
