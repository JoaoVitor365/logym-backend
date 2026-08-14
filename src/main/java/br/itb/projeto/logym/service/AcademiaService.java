package br.itb.projeto.logym.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Gerente;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;
import br.itb.projeto.logym.util.DocumentoValidator;

@Service
public class AcademiaService {

    private final AcademiaRepository academiaRepository;
    private final GerenteRepository gerenteRepository;
    private final UsuarioRepository usuarioRepository;

    public AcademiaService(
            AcademiaRepository academiaRepository,
            GerenteRepository gerenteRepository,
            UsuarioRepository usuarioRepository) {
        this.academiaRepository = academiaRepository;
        this.gerenteRepository = gerenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Academia> findAllAtivas() {
        return academiaRepository.findByStatusAcademia("ATIVO");
    }

    public List<Academia> findAll() {
        return academiaRepository.findAll();
    }

    public List<Academia> findByGerenteId(Long gerenteId) {
        return academiaRepository.findByGerenteId(gerenteId);
    }

    public List<Academia> findAtivasByGerenteId(Long gerenteId) {
        return academiaRepository.findByGerenteIdAndStatusAcademia(gerenteId, "ATIVO");
    }

    public Academia findById(Long id) {
        return academiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada."));
    }

    public List<Academia> findProximasPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String cepUsuario = limparCep(usuario.getCep());

        List<Academia> academias = academiaRepository.findByStatusAcademia("ATIVO");

        academias.sort(
                Comparator
                        .comparingInt(
                                (Academia academia) -> calcularPontuacaoCep(cepUsuario, limparCep(academia.getCep())))
                        .reversed());

        return academias;
    }

    public Academia create(Academia academia) {

        if (!DocumentoValidator.isValidCNPJ(academia.getCnpj())) {
            throw new RuntimeException("CNPJ inválido.");
        }

        academia.setCnpj(academia.getCnpj().replaceAll("\\D", ""));
        academia.setCep(limparCep(academia.getCep()));

        if (academia.getGerente() == null || academia.getGerente().getId() == null) {
            throw new RuntimeException("Gerente não informado.");
        }

        Gerente gerente = gerenteRepository.findById(academia.getGerente().getId())
                .orElseThrow(() -> new RuntimeException("Gerente não encontrado."));

        academia.setGerente(gerente);
        academia.setDataCadastro(LocalDateTime.now());
        academia.setStatusAcademia("ATIVO");

        // Academia nova ainda não possui avaliações.
        // Por isso a nota deve começar como NULL no banco.
        academia.setNota(null);

        return academiaRepository.save(academia);
    }

    public Academia update(Long id, Academia dadosAtualizados) {

        if (!DocumentoValidator.isValidCNPJ(dadosAtualizados.getCnpj())) {
            throw new RuntimeException("CNPJ inválido.");
        }

        dadosAtualizados.setCnpj(dadosAtualizados.getCnpj().replaceAll("\\D", ""));

        Academia academia = findById(id);

        academia.setNome(dadosAtualizados.getNome());
        academia.setCnpj(dadosAtualizados.getCnpj());
        academia.setDescricao(dadosAtualizados.getDescricao());
        academia.setCep(limparCep(dadosAtualizados.getCep()));
        academia.setEndereco(dadosAtualizados.getEndereco());
        academia.setNumero(dadosAtualizados.getNumero());
        academia.setComplemento(dadosAtualizados.getComplemento());
        academia.setBairro(dadosAtualizados.getBairro());
        academia.setCidade(dadosAtualizados.getCidade());
        academia.setEstado(dadosAtualizados.getEstado());
        academia.setTelefone(dadosAtualizados.getTelefone());
        academia.setCelular(dadosAtualizados.getCelular());
        academia.setEmail(dadosAtualizados.getEmail());
        academia.setCategorias(dadosAtualizados.getCategorias());
        academia.setFacilidades(dadosAtualizados.getFacilidades());

        // Não atualiza a nota aqui.
        // A nota deve ser controlada pelo fluxo de avaliações.

        return academiaRepository.save(academia);
    }

    /**
     * Fluxo do gerente: quando o próprio gerente inativa a academia.
     */
    public Academia inativar(Long id) {
        Academia academia = findById(id);

        if ("SUSPENSA".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Esta academia foi suspensa pela administração. Entre em contato com o suporte para reativar.");
        }

        academia.setStatusAcademia("INATIVO");

        return academiaRepository.save(academia);
    }

    /**
     * Fluxo do gerente: gerente só pode reativar academias INATIVAS.
     * Academia SUSPENSA só pode ser reativada pelo ADMIN.
     */
    public Academia reativar(Long id) {
        Academia academia = findById(id);

        if ("SUSPENSA".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Esta academia foi suspensa pela administração. Entre em contato com o suporte para reativar.");
        }

        academia.setStatusAcademia("ATIVO");

        return academiaRepository.save(academia);
    }

    /**
     * Fluxo do ADMIN: suspende a academia no banco como SUSPENSA.
     */
    public Academia suspenderAdmin(Long id) {
        Academia academia = findById(id);
        academia.setStatusAcademia("SUSPENSA");

        return academiaRepository.save(academia);
    }

    /**
     * Fluxo do ADMIN: pode reativar academias INATIVAS ou SUSPENSAS.
     */
    public Academia reativarAdmin(Long id) {
        Academia academia = findById(id);
        academia.setStatusAcademia("ATIVO");

        return academiaRepository.save(academia);
    }

    private String limparCep(String cep) {
        if (cep == null) {
            return "";
        }

        return cep.replaceAll("\\D", "");
    }

    private int calcularPontuacaoCep(String cepUsuario, String cepAcademia) {
        if (cepUsuario == null || cepAcademia == null) {
            return 0;
        }

        if (cepUsuario.length() != 8 || cepAcademia.length() != 8) {
            return 0;
        }

        int pontos = 0;

        for (int i = 0; i < 8; i++) {
            if (cepUsuario.charAt(i) == cepAcademia.charAt(i)) {
                pontos++;
            } else {
                break;
            }
        }

        return pontos;
    }
}
