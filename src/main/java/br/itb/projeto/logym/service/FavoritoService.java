package br.itb.projeto.logym.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Favorito;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.FavoritoRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AcademiaRepository academiaRepository;

    public FavoritoService(
            FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            AcademiaRepository academiaRepository
    ) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.academiaRepository = academiaRepository;
    }

    public boolean toggleFavorito(Long usuarioId, Long academiaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"USER".equals(usuario.getNivelAcesso())) {
            throw new RuntimeException("Apenas usuários comuns podem favoritar academias.");
        }

        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada."));

        if (!"ATIVO".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Não é possível favoritar uma academia inativa.");
        }

        Favorito favorito = favoritoRepository
                .findByUsuarioIdAndAcademiaId(usuarioId, academiaId)
                .orElse(null);

        if (favorito == null) {
            Favorito novoFavorito = new Favorito();
            novoFavorito.setUsuario(usuario);
            novoFavorito.setAcademia(academia);
            novoFavorito.setDataCadastro(LocalDateTime.now());
            novoFavorito.setStatusFavorito(true);

            favoritoRepository.save(novoFavorito);
            return true;
        }

        boolean novoStatus = !Boolean.TRUE.equals(favorito.getStatusFavorito());
        favorito.setStatusFavorito(novoStatus);
        favoritoRepository.save(favorito);

        return novoStatus;
    }

    public boolean isFavorito(Long usuarioId, Long academiaId) {
        return favoritoRepository
                .findByUsuarioIdAndAcademiaId(usuarioId, academiaId)
                .map(favorito -> Boolean.TRUE.equals(favorito.getStatusFavorito()))
                .orElse(false);
    }

    public List<Academia> findAcademiasFavoritas(Long usuarioId) {
        return favoritoRepository.findAcademiasFavoritasAtivasByUsuarioId(usuarioId);
    }
}