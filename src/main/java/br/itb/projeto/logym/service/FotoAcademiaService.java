package br.itb.projeto.logym.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.itb.projeto.logym.dto.FotoAcademiaDTO;
import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.FotoAcademia;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.FotoAcademiaRepository;

@Service
public class FotoAcademiaService {

    private final FotoAcademiaRepository fotoAcademiaRepository;
    private final AcademiaRepository academiaRepository;

    public FotoAcademiaService(
            FotoAcademiaRepository fotoAcademiaRepository,
            AcademiaRepository academiaRepository
    ) {
        this.fotoAcademiaRepository = fotoAcademiaRepository;
        this.academiaRepository = academiaRepository;
    }

    public FotoAcademiaDTO salvar(Long academiaId, MultipartFile arquivo) {
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada."));

        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Nenhuma foto foi enviada.");
        }

        String contentType = arquivo.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("O arquivo enviado precisa ser uma imagem.");
        }

        try {
            FotoAcademia fotoAcademia = new FotoAcademia();
            fotoAcademia.setAcademia(academia);
            fotoAcademia.setFoto(arquivo.getBytes());
            fotoAcademia.setTipoArquivo(contentType);
            fotoAcademia.setDataCadastro(LocalDateTime.now());
            fotoAcademia.setStatusFoto("ATIVO");

            FotoAcademia fotoSalva = fotoAcademiaRepository.save(fotoAcademia);

            return toDTO(fotoSalva);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar foto da academia.");
        }
    }

    public List<FotoAcademiaDTO> listarPorAcademia(Long academiaId) {
        return fotoAcademiaRepository
                .findByAcademiaIdAndStatusFotoOrderByDataCadastroDesc(academiaId, "ATIVO")
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public FotoAcademia buscarFoto(Long fotoId) {
        FotoAcademia foto = fotoAcademiaRepository.findById(fotoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada."));

        if (!"ATIVO".equals(foto.getStatusFoto())) {
            throw new RuntimeException("Foto não encontrada.");
        }

        return foto;
    }

    public void inativar(Long fotoId) {
        FotoAcademia foto = fotoAcademiaRepository.findById(fotoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada."));

        foto.setStatusFoto("INATIVO");
        fotoAcademiaRepository.save(foto);
    }

    private FotoAcademiaDTO toDTO(FotoAcademia fotoAcademia) {
        return new FotoAcademiaDTO(
                fotoAcademia.getId(),
                fotoAcademia.getAcademia().getId(),
                fotoAcademia.getTipoArquivo(),
                fotoAcademia.getDataCadastro()
        );
    }
}