package br.itb.projeto.logym.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import br.itb.projeto.logym.dto.FotoAcademiaDTO;
import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.FotoAcademia;
import br.itb.projeto.logym.model.entity.Gerente;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.FotoAcademiaRepository;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;

@Service
public class FotoAcademiaService {

    private final FotoAcademiaRepository fotoAcademiaRepository;
    private final AcademiaRepository academiaRepository;
    private final GerenteRepository gerenteRepository;
    private final UsuarioRepository usuarioRepository;

    public FotoAcademiaService(
            FotoAcademiaRepository fotoAcademiaRepository,
            AcademiaRepository academiaRepository,
            GerenteRepository gerenteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.fotoAcademiaRepository = fotoAcademiaRepository;
        this.academiaRepository = academiaRepository;
        this.gerenteRepository = gerenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public FotoAcademiaDTO salvar(Long academiaId, MultipartFile arquivo, Authentication authentication) {
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada."));

        validarPropriedadeAcademia(academia, authentication);

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

    public void inativar(Long fotoId, Authentication authentication) {
        FotoAcademia foto = fotoAcademiaRepository.findById(fotoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada."));

        validarPropriedadeAcademia(foto.getAcademia(), authentication);

        foto.setStatusFoto("INATIVO");
        fotoAcademiaRepository.save(foto);
    }

    private void validarPropriedadeAcademia(Academia academia, Authentication authentication) {
        Gerente gerenteAutenticado = buscarGerenteElegivel(authentication);

        if (academia.getGerente() == null
                || !gerenteAutenticado.getId().equals(academia.getGerente().getId())) {
            throw acessoNegado();
        }
    }

    private Gerente buscarGerenteElegivel(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw acessoNegado();
        }

        Usuario usuario = usuarioRepository.findByUsername(normalizarUsername(authentication.getName()))
                .orElseThrow(this::acessoNegado);

        if (!"MANAGER".equals(usuario.getNivelAcesso()) || !"ATIVO".equals(usuario.getStatusUsuario())) {
            throw acessoNegado();
        }

        Gerente gerente = gerenteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(this::acessoNegado);

        if (!"ATIVO".equals(gerente.getStatusGerente())) {
            throw acessoNegado();
        }

        return gerente;
    }

    private String normalizarUsername(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }

    private ResponseStatusException acessoNegado() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
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
