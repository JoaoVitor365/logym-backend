package br.itb.projeto.logym.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.itb.projeto.logym.dto.UsuarioDTO;
import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GerenteRepository gerenteRepository;
    private final AcademiaRepository academiaRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            GerenteRepository gerenteRepository,
            AcademiaRepository academiaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.gerenteRepository = gerenteRepository;
        this.academiaRepository = academiaRepository;
    }

    /* ================= LOGIN ================= */

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String usernameNormalizado = normalizarUsername(username);

        Usuario usuario = usuarioRepository.findByUsername(usernameNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        if ("SUSPENSO".equals(usuario.getStatusUsuario())) {
            throw new DisabledException("Sua conta foi suspensa pelo administrador. Entre em contato com o suporte.");
        }

        if (!"ATIVO".equals(usuario.getStatusUsuario())) {
            throw new DisabledException("Sua conta está inativa. Entre em contato com o suporte.");
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getNivelAcesso())
                .build();
    }

    /*
     * Verificação usada pelo front-end antes do POST /login.
     *
     * O Spring Security nem sempre devolve para o React a mensagem exata do
     * DisabledException. Por isso, este método permite que a tela de login mostre
     * mensagens específicas para contas INATIVAS e SUSPENSAS.
     */
    public Map<String, Object> verificarStatusLogin(String username) {
        String usernameNormalizado = normalizarUsername(username);

        if (usernameNormalizado == null || usernameNormalizado.isBlank()) {
            return Map.of(
                    "podeLogar", false,
                    "statusUsuario", "INVALIDO",
                    "message", "Informe o e-mail para continuar.");
        }

        Usuario usuario = usuarioRepository.findByUsername(usernameNormalizado)
                .orElse(null);

        /*
         * Não informamos que o e-mail não existe.
         * Assim, a mensagem continua genérica e mais segura.
         */
        if (usuario == null) {
            return Map.of(
                    "podeLogar", true,
                    "statusUsuario", "NAO_ENCONTRADO",
                    "message", "");
        }

        String status = usuario.getStatusUsuario();

        if ("SUSPENSO".equals(status)) {
            return Map.of(
                    "podeLogar", false,
                    "statusUsuario", "SUSPENSO",
                    "message", "Sua conta foi suspensa pelo administrador. Entre em contato com o suporte.");
        }

        if ("INATIVO".equals(status)) {
            return Map.of(
                    "podeLogar", false,
                    "statusUsuario", "INATIVO",
                    "message", "Sua conta está inativa. Entre em contato com o suporte.");
        }

        if ("TROCAR_SENHA".equals(status)) {
            return Map.of(
                    "podeLogar", false,
                    "statusUsuario", "TROCAR_SENHA",
                    "message", "Sua conta precisa trocar a senha. Use a opção de recuperação de senha.");
        }

        return Map.of(
                "podeLogar", true,
                "statusUsuario", "ATIVO",
                "message", "");
    }

    /* ================= CREATE ================= */

    public Usuario create(Usuario usuario) {

        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new RuntimeException("O nome é obrigatório.");
        }

        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new RuntimeException("O e-mail é obrigatório.");
        }

        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new RuntimeException("A senha é obrigatória.");
        }

        String usernameNormalizado = normalizarUsername(usuario.getUsername());

        if (usuarioRepository.existsByUsername(usernameNormalizado)) {
            throw new RuntimeException("Já existe uma conta cadastrada com este e-mail.");
        }

        Usuario novoUsuario = new Usuario();

        String nivelAcesso = usuario.getNivelAcesso() == null
                ? "USER"
                : usuario.getNivelAcesso();

        novoUsuario.setNome(usuario.getNome().trim());
        novoUsuario.setUsername(usernameNormalizado);
        novoUsuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        novoUsuario.setStatusUsuario("ATIVO");
        novoUsuario.setDataCadastro(LocalDateTime.now());
        novoUsuario.setNivelAcesso(nivelAcesso);

        if ("USER".equals(nivelAcesso)) {
            novoUsuario.setCep(limparCep(usuario.getCep()));
        } else {
            novoUsuario.setCep(null);
        }

        try {
            return usuarioRepository.save(novoUsuario);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Já existe uma conta cadastrada com este e-mail.");
        }
    }

    /* ================= EDITAR PERFIL ================= */

    public Usuario editar(MultipartFile file, Long id, Usuario usuario) {

        Usuario usuarioAtual = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getNome() != null && !usuario.getNome().isBlank()) {
            usuarioAtual.setNome(usuario.getNome().trim());
        }

        // Não altera username/e-mail aqui.

        if ("USER".equals(usuarioAtual.getNivelAcesso())) {
            String cepLimpo = limparCep(usuario.getCep());

            if (cepLimpo != null && !cepLimpo.isBlank() && cepLimpo.length() != 8) {
                throw new RuntimeException("CEP inválido. Informe 8 dígitos.");
            }

            usuarioAtual.setCep(cepLimpo);
        }

        usuarioAtual.setDataAtualizacao(LocalDateTime.now());

        if (file != null && file.getSize() > 0) {
            try {
                usuarioAtual.setFoto(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar a foto do usuário", e);
            }
        }

        return usuarioRepository.save(usuarioAtual);
    }

    /* ================= ALTERAR FOTO DE PERFIL ================= */

    public void atualizarFoto(Long id, MultipartFile file) {
        Usuario usuarioAtual = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        /*
         * Valida se realmente veio um arquivo na requisição.
         * O Mobile envia a imagem no campo "file".
         */
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Nenhuma foto foi enviada.");
        }

        try {
            /*
             * Converte a imagem enviada em bytes.
             * Esses bytes são salvos no campo foto da tabela Usuario.
             */
            usuarioAtual.setFoto(file.getBytes());

            /*
             * Atualiza a data de alteração do usuário.
             */
            usuarioAtual.setDataAtualizacao(LocalDateTime.now());

            /*
             * Salva a foto nova no banco.
             * Depois disso, o Web e o Mobile conseguem buscar a mesma foto
             * pela rota GET /usuarios/{id}/foto.
             */
            usuarioRepository.save(usuarioAtual);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar a foto do usuário", e);
        }
    }

    /* ================= ALTERAR SENHA ================= */

    public Usuario alterarSenha(Long id, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuario.setStatusUsuario("ATIVO");
        usuario.setDataAtualizacao(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    /* ================= INATIVAR ================= */

    @Transactional
    public Usuario inativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setStatusUsuario("INATIVO");
        usuario.setDataAtualizacao(LocalDateTime.now());

        inativarGerenteEAcademias(usuario);

        return usuarioRepository.save(usuario);
    }

    /* ================= SUSPENDER ADMIN ================= */

    /**
     * Fluxo do ADMIN: suspende o usuário no banco como SUSPENSO.
     *
     * Importante:
     * - INATIVO deve ser usado quando o próprio usuário inativa a conta.
     * - SUSPENSO deve ser usado quando o ADMIN suspende a conta.
     */
    @Transactional
    public Usuario suspender(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setStatusUsuario("SUSPENSO");
        usuario.setDataAtualizacao(LocalDateTime.now());

        suspenderGerenteEAcademias(usuario);

        return usuarioRepository.save(usuario);
    }

    /* ================= ATIVAR ================= */

    @Transactional
    public Usuario ativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String statusAnterior = usuario.getStatusUsuario();

        usuario.setStatusUsuario("ATIVO");
        usuario.setDataAtualizacao(LocalDateTime.now());

        ativarGerenteEAcademias(usuario, statusAnterior);

        return usuarioRepository.save(usuario);
    }

    /* ================= BUSCAS ================= */

    public UsuarioDTO findByUsername(Authentication authentication) {
        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return toDTO(usuario);
    }

    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UsuarioDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return toDTO(usuario);
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getNivelAcesso(),
                usuario.getCep(),
                usuario.getFoto(),
                usuario.getDataCadastro(),
                usuario.getStatusUsuario());
    }

    private void inativarGerenteEAcademias(Usuario usuario) {
        if (!"MANAGER".equals(usuario.getNivelAcesso())) {
            return;
        }

        gerenteRepository.findByUsuarioId(usuario.getId())
                .ifPresent(gerente -> {
                    gerente.setStatusGerente("INATIVO");
                    gerenteRepository.save(gerente);

                    List<Academia> academias = academiaRepository.findByGerenteId(gerente.getId());

                    academias.stream()
                            .filter(academia -> "ATIVO".equals(academia.getStatusAcademia()))
                            .forEach(academia -> {
                                academia.setStatusAnteriorBloqueioGerente("ATIVO");
                                academia.setStatusAcademia("INATIVO");
                            });

                    academiaRepository.saveAll(academias);
                });
    }

    private void suspenderGerenteEAcademias(Usuario usuario) {
        if (!"MANAGER".equals(usuario.getNivelAcesso())) {
            return;
        }

        gerenteRepository.findByUsuarioId(usuario.getId())
                .ifPresent(gerente -> {
                    gerente.setStatusGerente("INATIVO");
                    gerenteRepository.save(gerente);

                    List<Academia> academias = academiaRepository.findByGerenteId(gerente.getId());

                    academias.stream()
                            .filter(academia -> "ATIVO".equals(academia.getStatusAcademia())
                                    || "INATIVO".equals(academia.getStatusAcademia()))
                            .forEach(academia -> {
                                academia.setStatusAnteriorBloqueioGerente(academia.getStatusAcademia());
                                academia.setStatusAcademia("SUSPENSA");
                            });

                    academiaRepository.saveAll(academias);
                });
    }

    private void ativarGerenteEAcademias(Usuario usuario, String statusAnterior) {
        if (!"MANAGER".equals(usuario.getNivelAcesso())) {
            return;
        }

        gerenteRepository.findByUsuarioId(usuario.getId())
                .ifPresent(gerente -> {
                    gerente.setStatusGerente("ATIVO");
                    gerenteRepository.save(gerente);

                    List<Academia> academias = academiaRepository.findByGerenteId(gerente.getId());

                    if ("INATIVO".equals(statusAnterior)) {
                        academias.stream()
                                .filter(academia -> "INATIVO".equals(academia.getStatusAcademia()))
                                .filter(academia -> academia.getStatusAnteriorBloqueioGerente() != null)
                                .forEach(academia -> {
                                    academia.setStatusAcademia(academia.getStatusAnteriorBloqueioGerente());
                                    academia.setStatusAnteriorBloqueioGerente(null);
                                });
                    }

                    if ("SUSPENSO".equals(statusAnterior)) {
                        academias.stream()
                                .filter(academia -> "SUSPENSA".equals(academia.getStatusAcademia()))
                                .filter(academia -> academia.getStatusAnteriorBloqueioGerente() != null)
                                .forEach(academia -> {
                                    academia.setStatusAcademia(academia.getStatusAnteriorBloqueioGerente());
                                    academia.setStatusAnteriorBloqueioGerente(null);
                                });
                    }

                    academiaRepository.saveAll(academias);
                });
    }

    private String limparCep(String cep) {
        if (cep == null) {
            return null;
        }

        return cep.replaceAll("\\D", "");
    }

    private String normalizarUsername(String username) {
        if (username == null) {
            return null;
        }

        return username.trim().toLowerCase();
    }
}
