package br.itb.projeto.logym.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import br.itb.projeto.logym.dto.AcademiaProximaDTO;
import br.itb.projeto.logym.dto.AcademiaComparacaoDTO;
import br.itb.projeto.logym.dto.PaginaAcademiasDTO;
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

    @GetMapping("/home")
    public ResponseEntity<PaginaAcademiasDTO> findParaHome(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> categorias,
            @RequestParam(required = false) List<Long> facilidades,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.findAtivasParaHome(
                page, search, categorias, facilidades, authentication));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Academia>> findAllAdmin() {
        return ResponseEntity.ok(academiaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Academia> findById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.findById(id, authentication));
    }

    @GetMapping("/gerente/{gerenteId}")
    public ResponseEntity<List<Academia>> findByGerenteId(
            @PathVariable Long gerenteId,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.findByGerenteId(gerenteId, authentication));
    }

    @GetMapping("/proximas")
    public ResponseEntity<List<AcademiaProximaDTO>> findProximas(Authentication authentication) {
        return ResponseEntity.ok(academiaService.findProximasPorUsuario(authentication));
    }

    @GetMapping("/comparar")
    public ResponseEntity<List<AcademiaComparacaoDTO>> comparar(
            @RequestParam("ids") List<Long> ids,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.comparar(ids, authentication));
    }

    @GetMapping("/proximas/usuario/{usuarioId}")
    public ResponseEntity<List<AcademiaProximaDTO>> findProximasPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.findProximasPorUsuario(usuarioId, authentication));
    }

    @PostMapping(value = { "", "/" }, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Academia> create(
            @RequestBody Academia academia,
            Authentication authentication) {
        Academia novaAcademia = academiaService.create(academia, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaAcademia);
    }

    @PostMapping(value = { "", "/" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Academia> createComFotos(
            @RequestPart("academia") Academia academia,
            @RequestPart(name = "foto", required = false) List<MultipartFile> fotos,
            @RequestParam(required = false) Integer fotoPrincipalIndex,
            Authentication authentication) {
        Academia novaAcademia = academiaService.createComFotos(
                academia, fotos, fotoPrincipalIndex, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaAcademia);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Academia> update(
            @PathVariable Long id,
            @RequestBody Academia academia,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.update(id, academia, authentication));
    }

    /**
     * Fluxo do GERENTE: inativa a própria academia.
     */
    @PutMapping("/{id}/inativar")
    public ResponseEntity<Academia> inativar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(academiaService.inativar(id, authentication));
    }

    /**
     * Fluxo do GERENTE: reativa apenas academia INATIVA.
     * Se estiver SUSPENSA, o service bloqueia.
     */
    @PutMapping("/{id}/reativar")
    public ResponseEntity<Academia> reativar(
            @PathVariable Long id,
            Authentication authentication) {
        Academia academia = academiaService.reativar(id, authentication);
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
