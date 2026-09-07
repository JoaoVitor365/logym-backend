package br.itb.projeto.logym.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.logym.dto.CoordenadasDTO;
import br.itb.projeto.logym.dto.EnderecoGeocodificacaoDTO;
import br.itb.projeto.logym.dto.UsuarioDTO;
import br.itb.projeto.logym.dto.UsuarioRequestDTO;
import br.itb.projeto.logym.exception.GeocodificacaoException;
import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaService senhaService;
    private final GerenteRepository gerenteRepository;
    private final AcademiaRepository academiaRepository;
    private final GeocodificacaoService geocodificacaoService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            SenhaService senhaService,
            GerenteRepository gerenteRepository,
            AcademiaRepository academiaRepository,
            GeocodificacaoService geocodificacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaService = senhaService;
        this.gerenteRepository = gerenteRepository;
        this.academiaRepository = academiaRepository;
        this.geocodificacaoService = geocodificacaoService;
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

    @Transactional
    public Usuario create(UsuarioRequestDTO usuario) {

        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new RuntimeException("O nome é obrigatório.");
        }

        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new RuntimeException("O e-mail é obrigatório.");
        }

        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new RuntimeException("A senha é obrigatória.");
        }

        senhaService.validarSenha(usuario.getPassword());

        String usernameNormalizado = normalizarUsername(usuario.getUsername());

        if (usuarioRepository.existsByUsername(usernameNormalizado)) {
            throw new RuntimeException("Já existe uma conta cadastrada com este e-mail.");
        }

        Usuario novoUsuario = new Usuario();

        String nivelAcesso = "USER";

        novoUsuario.setNome(usuario.getNome().trim());
        novoUsuario.setUsername(usernameNormalizado);
        novoUsuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        novoUsuario.setStatusUsuario("ATIVO");
        novoUsuario.setDataCadastro(LocalDateTime.now());
        novoUsuario.setNivelAcesso(nivelAcesso);

        if ("USER".equals(nivelAcesso)) {
            novoUsuario.setCep(limparCep(usuario.getCep()));
            validarNumero(usuario.getNumero());
            novoUsuario.setNumero(usuario.getNumero());
            novoUsuario.setComplemento(usuario.getComplemento());

            if (possuiEnderecoCompleto(usuario, novoUsuario.getCep(), novoUsuario.getNumero())) {
                CoordenadasDTO coordenadas = geocodificarUsuario(usuario, novoUsuario.getCep(), novoUsuario.getNumero());
                novoUsuario.setLatitude(coordenadas.latitude());
                novoUsuario.setLongitude(coordenadas.longitude());
            }
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

    @Transactional
    public Usuario editar(MultipartFile file, Long id, UsuarioRequestDTO usuario) {

        Usuario usuarioAtual = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getNome() != null && !usuario.getNome().isBlank()) {
            usuarioAtual.setNome(usuario.getNome().trim());
        }

        // Não altera username/e-mail aqui.

        if ("USER".equals(usuarioAtual.getNivelAcesso())) {
            String cepAtualizado = usuario.getCep() == null
                    ? usuarioAtual.getCep()
                    : limparCep(usuario.getCep());
            BigDecimal numeroAtualizado = usuario.getNumero() == null
                    ? usuarioAtual.getNumero()
                    : usuario.getNumero();

            if (usuario.getCep() != null && !cepAtualizado.isBlank() && cepAtualizado.length() != 8) {
                throw new RuntimeException("CEP inválido. Informe 8 dígitos.");
            }

            validarNumero(usuario.getNumero());

            boolean enderecoAlterado = !Objects.equals(limparCep(usuarioAtual.getCep()), cepAtualizado)
                    || numerosDiferentes(usuarioAtual.getNumero(), numeroAtualizado);

            CoordenadasDTO coordenadas = null;
            if (enderecoAlterado) {
                if (!possuiEnderecoCompleto(usuario, cepAtualizado, numeroAtualizado)) {
                    throw new GeocodificacaoException(
                            "Endereco completo necessario para atualizar CEP ou numero.");
                }

                coordenadas = geocodificarUsuario(usuario, cepAtualizado, numeroAtualizado);
            }

            if (usuario.getCep() != null) {
                usuarioAtual.setCep(cepAtualizado);
            }

            if (usuario.getNumero() != null) {
                usuarioAtual.setNumero(numeroAtualizado);
            }

            if (usuario.getComplemento() != null) {
                usuarioAtual.setComplemento(usuario.getComplemento());
            }

            if (coordenadas != null) {
                usuarioAtual.setLatitude(coordenadas.latitude());
                usuarioAtual.setLongitude(coordenadas.longitude());
            }
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

        senhaService.validarSenha(novaSenha);
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

    public void validarProprietario(Long id, Authentication authentication) {
        Usuario usuarioAutenticado = buscarUsuarioAutenticado(authentication);

        if (!usuarioAutenticado.getId().equals(id)) {
            throw acessoNegado();
        }
    }

    public void validarLeituraUsuario(Long id, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return;
        }

        validarProprietario(id, authentication);
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getNivelAcesso(),
                usuario.getCep(),
                usuario.getNumero(),
                usuario.getComplemento(),
                usuario.getLatitude(),
                usuario.getLongitude(),
                usuario.getFoto(),
                usuario.getDataCadastro(),
                usuario.getStatusUsuario());
    }

    private Usuario buscarUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw acessoNegado();
        }

        return usuarioRepository.findByUsername(normalizarUsername(authentication.getName()))
                .orElseThrow(this::acessoNegado);
    }

    private ResponseStatusException acessoNegado() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
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

    private CoordenadasDTO geocodificarUsuario(
            UsuarioRequestDTO usuario,
            String cep,
            BigDecimal numero) {
        return geocodificacaoService.geocodificar(new EnderecoGeocodificacaoDTO(
                usuario.getEndereco(),
                numero,
                usuario.getBairro(),
                usuario.getCidade(),
                usuario.getEstado(),
                cep,
                usuario.getComplemento()));
    }

    private boolean possuiEnderecoCompleto(
            UsuarioRequestDTO usuario,
            String cep,
            BigDecimal numero) {
        return textoPreenchido(usuario.getEndereco())
                && textoPreenchido(usuario.getBairro())
                && textoPreenchido(usuario.getCidade())
                && textoPreenchido(usuario.getEstado())
                && textoPreenchido(cep)
                && numero != null;
    }

    private boolean textoPreenchido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private boolean numerosDiferentes(BigDecimal numeroAtual, BigDecimal numeroAtualizado) {
        if (numeroAtual == null || numeroAtualizado == null) {
            return numeroAtual != numeroAtualizado;
        }

        return numeroAtual.compareTo(numeroAtualizado) != 0;
    }

    private void validarNumero(BigDecimal numero) {
        if (numero != null && numero.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Numero invalido. Informe um valor nao negativo.");
        }
    }

    private String normalizarUsername(String username) {
        if (username == null) {
            return null;
        }

        return username.trim().toLowerCase();
    }
}
