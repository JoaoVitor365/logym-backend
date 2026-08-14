package br.itb.projeto.logym.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import br.itb.projeto.logym.model.entity.Gerente;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;
import br.itb.projeto.logym.util.DocumentoValidator;

@Service
public class GerenteService {

    private final GerenteRepository gerenteRepository;
    private final UsuarioRepository usuarioRepository;

    public GerenteService(
            GerenteRepository gerenteRepository,
            UsuarioRepository usuarioRepository) {
        this.gerenteRepository = gerenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Gerente create(Gerente gerente) {

        if (gerente.getCpf() == null || gerente.getCpf().trim().isEmpty()) {
            throw new RuntimeException("O CPF é obrigatório.");
        }

        String cpfLimpo = gerente.getCpf().replaceAll("\\D", "");

        if (!DocumentoValidator.isValidCPF(cpfLimpo)) {
            throw new RuntimeException("CPF inválido.");
        }

        if (gerenteRepository.existsByCpf(cpfLimpo)) {
            throw new RuntimeException("Já existe um gerente cadastrado com este CPF.");
        }

        if (gerente.getUsuario() == null || gerente.getUsuario().getId() == null) {
            throw new RuntimeException("Usuário não informado.");
        }

        Long usuarioId = gerente.getUsuario().getId();

        Optional<Gerente> gerenteExistente = gerenteRepository.findByUsuarioId(usuarioId);

        if (gerenteExistente.isPresent()) {
            throw new RuntimeException("Este usuário já possui cadastro de gerente.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"MANAGER".equals(usuario.getNivelAcesso())) {
            throw new RuntimeException("Este usuário não possui permissão de gerente.");
        }

        gerente.setCpf(cpfLimpo);
        gerente.setUsuario(usuario);
        gerente.setNome(usuario.getNome());
        gerente.setDataCadastro(LocalDateTime.now());
        gerente.setStatusGerente("ATIVO");

        try {
            return gerenteRepository.save(gerente);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Já existe um gerente cadastrado com este CPF.");
        }
    }

    public Optional<Gerente> findByUsuarioId(Long usuarioId) {
        return gerenteRepository.findByUsuarioId(usuarioId);
    }

    public List<Gerente> findAll() {
        return gerenteRepository.findAll();
    }

    public Gerente findById(Long id) {
        return gerenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));
    }
}