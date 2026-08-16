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

import br.itb.projeto.logym.model.entity.Categoria;
import br.itb.projeto.logym.service.CategoriaService;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping({ "", "/", "/ativas" })
    public ResponseEntity<List<Categoria>> findAllAtivas() {
        return ResponseEntity.ok(categoriaService.findAllAtivas());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Categoria>> findAll() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    @PostMapping("/admin")
    public ResponseEntity<Categoria> create(@RequestBody Categoria categoria) {
        Categoria novaCategoria = categoriaService.create(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCategoria);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Categoria> update(
            @PathVariable Long id,
            @RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.update(id, categoria));
    }

    @PutMapping("/admin/{id}/inativar")
    public ResponseEntity<Categoria> inativar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.inativar(id));
    }

    @PutMapping("/admin/{id}/reativar")
    public ResponseEntity<Categoria> reativar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.reativar(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.findById(id));
    }
}
