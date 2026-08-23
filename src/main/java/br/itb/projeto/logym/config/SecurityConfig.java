package br.itb.projeto.logym.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/usuarios", "/usuarios/create").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/verificar-status-login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/*/suspender", "/usuarios/*/ativar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/recuperar-senha/solicitar-codigo",
                                "/recuperar-senha/validar-codigo",
                                "/recuperar-senha/redefinir-senha").permitAll()

                        // Academias
                        .requestMatchers("/academias/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/academias/gerente/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/academias", "/academias/").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/academias/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/academias/*/inativar", "/academias/*/reativar")
                        .hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/academias", "/academias/", "/academias/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/academias/proximas/usuario/*").permitAll()
                        .requestMatchers("/categorias/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/", "/categorias/ativas").permitAll()
                        .requestMatchers("/facilidades/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/facilidades", "/facilidades/", "/facilidades/ativas").permitAll()
                        // Usuário / perfil / foto
                        // Avaliações
                        .requestMatchers("/avaliacoes/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/itens").permitAll()
                        .requestMatchers(HttpMethod.GET, "/avaliacoes/academia/*").permitAll()
                        // Fotos das academias
                        .requestMatchers(HttpMethod.POST, "/fotos-academia/*").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/fotos-academia/*/inativar").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/fotos-academia/academia/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fotos-academia/*/imagem").permitAll()

                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");

                            response.getWriter().write("""
                                        {
                                          "message": "Login realizado com sucesso"
                                        }
                                    """);
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");

                            String username = request.getParameter("username");

                            if (username == null || username.isBlank()) {
                                username = request.getParameter("email");
                            }

                            if (username != null && !username.isBlank()) {
                                Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(username);

                                if (usuarioOptional.isPresent()) {
                                    Usuario usuario = usuarioOptional.get();

                                    if ("INATIVO".equals(usuario.getStatusUsuario())) {
                                        response.getWriter()
                                                .write("""
                                                            {
                                                              "message": "Sua conta está inativada. Entre em contato com o suporte."
                                                            }
                                                        """);
                                        return;
                                    }
                                }
                            }

                            response.getWriter().write("""
                                        {
                                          "message": "E-mail ou senha inválidos."
                                        }
                                    """);
                        }))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");

                            response.getWriter().write("""
                                        {
                                          "message": "Logout realizado com sucesso"
                                        }
                                    """);
                        }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
