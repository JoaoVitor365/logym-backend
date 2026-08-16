package br.itb.projeto.logym.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.logym.model.entity.Facilidade;
import br.itb.projeto.logym.service.FacilidadeService;

@RestController
@RequestMapping("/facilidades")
public class FacilidadeController {

    private final FacilidadeService facilidadeService;

    public FacilidadeController(FacilidadeService facilidadeService) {
        this.facilidadeService = facilidadeService;
    }

    @GetMapping({ "", "/", "/ativas" })
    public ResponseEntity<List<Facilidade>> findAllAtivas() {
        return ResponseEntity.ok(facilidadeService.findAllAtivas());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Facilidade>> findAll() {
        return ResponseEntity.ok(facilidadeService.findAll());
    }

    @PostMapping("/admin")
    public ResponseEntity<Facilidade> create(@RequestBody Facilidade facilidade) {
        Facilidade novaFacilidade = facilidadeService.create(facilidade);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaFacilidade);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Facilidade> update(
            @PathVariable Long id,
            @RequestBody Facilidade facilidade) {
        return ResponseEntity.ok(facilidadeService.update(id, facilidade));
    }

    @PutMapping("/admin/{id}/inativar")
    public ResponseEntity<Facilidade> inativar(@PathVariable Long id) {
        return ResponseEntity.ok(facilidadeService.inativar(id));
    }

    @PutMapping("/admin/{id}/reativar")
    public ResponseEntity<Facilidade> reativar(@PathVariable Long id) {
        return ResponseEntity.ok(facilidadeService.reativar(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facilidade> findById(@PathVariable Long id) {
        return ResponseEntity.ok(facilidadeService.findById(id));
    }
}
