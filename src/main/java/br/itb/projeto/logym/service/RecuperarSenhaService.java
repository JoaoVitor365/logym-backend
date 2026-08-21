package br.itb.projeto.logym.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.logym.model.entity.RecuperarSenha;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.RecuperarSenhaRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;

@Service
public class RecuperarSenhaService {

    private static final String MENSAGEM_SOLICITACAO =
            "Se o e-mail informado estiver cadastrado e apto para recuperacao, enviaremos um codigo.";

    private final RecuperarSenhaRepository recuperarSenhaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaService senhaService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperarSenhaService(
            RecuperarSenhaRepository recuperarSenhaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            SenhaService senhaService,
            EmailService emailService) {
        this.recuperarSenhaRepository = recuperarSenhaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaService = senhaService;
        this.emailService = emailService;
    }

    @Transactional(noRollbackFor = EmailEnvioException.class)
    public String solicitarCodigo(String email) {
        String emailNormalizado = normalizarEmail(email);

        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            return MENSAGEM_SOLICITACAO;
        }

        Usuario usuario = usuarioRepository.findByUsername(emailNormalizado).orElse(null);

        if (usuario == null || !usuarioPodeRecuperarSenha(usuario)) {
            return MENSAGEM_SOLICITACAO;
        }

        List<RecuperarSenha> codigosAtivos = recuperarSenhaRepository
                .findByEmailAndStatusCodigoTrue(emailNormalizado);
        codigosAtivos.forEach(codigoAnterior -> codigoAnterior.setStatusCodigo(false));
        recuperarSenhaRepository.saveAll(codigosAtivos);

        LocalDateTime geradoEm = LocalDateTime.now();
        RecuperarSenha recuperarSenha = new RecuperarSenha();
        recuperarSenha.setEmail(emailNormalizado);
        recuperarSenha.setCodigo(gerarCodigo());
        recuperarSenha.setGeradoEm(geradoEm);
        recuperarSenha.setExpiraEm(geradoEm.plusMinutes(15));
        recuperarSenha.setStatusCodigo(true);

        RecuperarSenha codigoSalvo = recuperarSenhaRepository.save(recuperarSenha);

        try {
            emailService.enviarCodigoRecuperacao(emailNormalizado, codigoSalvo.getCodigo());
        } catch (RuntimeException e) {
            codigoSalvo.setStatusCodigo(false);
            recuperarSenhaRepository.save(codigoSalvo);
            throw new EmailEnvioException("Nao foi possivel enviar o codigo de recuperacao.", e);
        }

        return MENSAGEM_SOLICITACAO;
    }

    public void validarCodigo(String email, String codigo) {
        localizarCodigoValido(email, codigo);
    }

    @Transactional
    public void redefinirSenha(String email, String codigo, String novaSenha) {
        String emailNormalizado = normalizarEmail(email);
        RecuperarSenha recuperarSenha = localizarCodigoValido(emailNormalizado, codigo);

        Usuario usuario = usuarioRepository.findByUsername(emailNormalizado)
                .orElseThrow(() -> new RuntimeException("Codigo invalido ou expirado."));

        if (!usuarioPodeRecuperarSenha(usuario)) {
            throw new RuntimeException("Codigo invalido ou expirado.");
        }

        senhaService.validarSenha(novaSenha);
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);

        recuperarSenha.setStatusCodigo(false);
        recuperarSenhaRepository.save(recuperarSenha);
    }

    private RecuperarSenha localizarCodigoValido(String email, String codigo) {
        String emailNormalizado = normalizarEmail(email);

        if (emailNormalizado == null || emailNormalizado.isBlank() || codigo == null || codigo.isBlank()) {
            throw new RuntimeException("Codigo invalido ou expirado.");
        }

        RecuperarSenha recuperarSenha = recuperarSenhaRepository
                .findFirstByEmailAndCodigoAndStatusCodigoTrueOrderByGeradoEmDesc(emailNormalizado, codigo)
                .orElseThrow(() -> new RuntimeException("Codigo invalido ou expirado."));

        if (!recuperarSenha.getExpiraEm().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Codigo invalido ou expirado.");
        }

        return recuperarSenha;
    }

    private boolean usuarioPodeRecuperarSenha(Usuario usuario) {
        return "ATIVO".equals(usuario.getStatusUsuario())
                || "TROCAR_SENHA".equals(usuario.getStatusUsuario());
    }

    private String gerarCodigo() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String normalizarEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
