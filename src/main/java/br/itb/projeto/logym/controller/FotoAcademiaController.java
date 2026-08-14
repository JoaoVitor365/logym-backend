package br.itb.projeto.logym.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import br.itb.projeto.logym.dto.FotoAcademiaDTO;
import br.itb.projeto.logym.model.entity.FotoAcademia;
import br.itb.projeto.logym.service.FotoAcademiaService;

@RestController
@RequestMapping("/fotos-academia")
public class FotoAcademiaController {

    private final FotoAcademiaService fotoAcademiaService;

    public FotoAcademiaController(FotoAcademiaService fotoAcademiaService) {
        this.fotoAcademiaService = fotoAcademiaService;
    }

    @PostMapping("/{academiaId}")
    public ResponseEntity<FotoAcademiaDTO> salvar(
            @PathVariable Long academiaId,
            @RequestParam("foto") MultipartFile foto
    ) {
        return ResponseEntity.ok(fotoAcademiaService.salvar(academiaId, foto));
    }

    @GetMapping("/academia/{academiaId}")
    public ResponseEntity<List<FotoAcademiaDTO>> listarPorAcademia(@PathVariable Long academiaId) {
        return ResponseEntity.ok(fotoAcademiaService.listarPorAcademia(academiaId));
    }

    @GetMapping("/{fotoId}/imagem")
    public ResponseEntity<byte[]> buscarImagem(@PathVariable Long fotoId) {
        FotoAcademia foto = fotoAcademiaService.buscarFoto(fotoId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(foto.getTipoArquivo()))
                .body(foto.getFoto());
    }

    @PutMapping("/{fotoId}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long fotoId) {
        fotoAcademiaService.inativar(fotoId);
        return ResponseEntity.noContent().build();
    }
}