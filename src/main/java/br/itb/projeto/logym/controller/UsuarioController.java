package br.itb.projeto.logym.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.itb.projeto.logym.dto.UsuarioDTO;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping({ "", "/create" })
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        Usuario createdUsuario = usuarioService.create(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsuario);
    }

    /*
     * Usado pela tela de login antes de tentar autenticar.
     *
     * Objetivo:
     * - Se a conta estiver SUSPENSO, mostrar mensagem clara para o usuário.
     * - Se a conta estiver INATIVO, mostrar mensagem clara para o usuário.
     * - Se a conta estiver ATIVO, permitir continuar o login normal.
     */
    @GetMapping("/verificar-status-login")
    public ResponseEntity<Map<String, Object>> verificarStatusLogin(@RequestParam String username) {
        return ResponseEntity.ok(usuarioService.verificarStatusLogin(username));
    }

    /*
     * Edição completa por multipart/form-data.
     *
     * Essa rota já era usada pelo Web quando precisa enviar dados do usuário
     * junto com uma foto.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Usuario> editar(
            @PathVariable Long id,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart Usuario usuario) {

        Usuario usuarioAtualizado = usuarioService.editar(file, id, usuario);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    /*
     * Edição por JSON.
     *
     * Essa rota é usada pelo Mobile para atualizar nome e CEP.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Usuario> editarJson(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        Usuario usuarioAtualizado = usuarioService.editar(null, id, usuario);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    /*
     * Rota específica para o Mobile atualizar apenas a foto de perfil.
     *
     * O Mobile envia multipart/form-data com o campo:
     * file = imagem escolhida na galeria
     *
     * Rota chamada pelo Mobile:
     * PUT /usuarios/{id}/foto
     */
    @PutMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> atualizarFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        usuarioService.atualizarFoto(id, file);

        /*
         * 204 No Content significa:
         * deu certo, mas não precisa retornar objeto no corpo da resposta.
         */
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/alterar-senha")
    public ResponseEntity<Usuario> alterarSenha(
            @PathVariable Long id,
            @RequestParam String novaSenha) {

        Usuario usuario = usuarioService.alterarSenha(id, novaSenha);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/inativar")
    public ResponseEntity<Usuario> inativar(@PathVariable Long id) {
        Usuario usuario = usuarioService.inativar(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/suspender")
    public ResponseEntity<Usuario> suspender(@PathVariable Long id) {
        Usuario usuario = usuarioService.suspender(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/ativar")
    public ResponseEntity<Usuario> ativar(@PathVariable Long id) {
        Usuario usuario = usuarioService.ativar(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/me")
    public UsuarioDTO me(Authentication authentication) {
        UsuarioDTO usuario = usuarioService.findByUsername(authentication);
        return usuario;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    /*
     * Busca a foto de perfil do usuário.
     *
     * Essa rota já era usada pelo Web e pelo Mobile para exibir a foto.
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable Long id) {

        UsuarioDTO usuario = usuarioService.findById(id);

        if (usuario.getFoto() == null || usuario.getFoto().length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(usuario.getFoto());
    }

}