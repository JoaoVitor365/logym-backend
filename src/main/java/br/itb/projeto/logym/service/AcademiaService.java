package br.itb.projeto.logym.service;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.logym.dto.CoordenadasDTO;
import br.itb.projeto.logym.dto.EnderecoGeocodificacaoDTO;
import br.itb.projeto.logym.dto.AcademiaProximaDTO;
import br.itb.projeto.logym.dto.AcademiaComparacaoDTO;
import br.itb.projeto.logym.dto.FotoAcademiaDTO;
import br.itb.projeto.logym.dto.PaginaAcademiasDTO;
import br.itb.projeto.logym.exception.GeocodificacaoException;
import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Categoria;
import br.itb.projeto.logym.model.entity.CategoriaAcademia;
import br.itb.projeto.logym.model.entity.Facilidade;
import br.itb.projeto.logym.model.entity.FacilidadeAcademia;
import br.itb.projeto.logym.model.entity.Gerente;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.CategoriaAcademiaRepository;
import br.itb.projeto.logym.repository.CategoriaRepository;
import br.itb.projeto.logym.repository.FacilidadeAcademiaRepository;
import br.itb.projeto.logym.repository.FacilidadeRepository;
import br.itb.projeto.logym.repository.FotoAcademiaRepository;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.ItemAvaliacaoAcademiaRepository;
import br.itb.projeto.logym.repository.ItemAvaliacaoRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;
import br.itb.projeto.logym.util.DocumentoValidator;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AcademiaService {

    private static final double RAIO_PADRAO_KM = 5.0;
    private static final double RAIO_TERRA_KM = 6_371.0;
    private static final int TAMANHO_PAGINA_HOME = 16;

    private final AcademiaRepository academiaRepository;
    private final GerenteRepository gerenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaAcademiaRepository categoriaAcademiaRepository;
    private final FacilidadeRepository facilidadeRepository;
    private final FacilidadeAcademiaRepository facilidadeAcademiaRepository;
    private final FotoAcademiaRepository fotoAcademiaRepository;
    private final ItemAvaliacaoRepository itemAvaliacaoRepository;
    private final ItemAvaliacaoAcademiaRepository itemAvaliacaoAcademiaRepository;
    private final GeocodificacaoService geocodificacaoService;
    private final FotoAcademiaService fotoAcademiaService;

    public AcademiaService(
            AcademiaRepository academiaRepository,
            GerenteRepository gerenteRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            CategoriaAcademiaRepository categoriaAcademiaRepository,
            FacilidadeRepository facilidadeRepository,
            FacilidadeAcademiaRepository facilidadeAcademiaRepository,
            FotoAcademiaRepository fotoAcademiaRepository,
            ItemAvaliacaoRepository itemAvaliacaoRepository,
            ItemAvaliacaoAcademiaRepository itemAvaliacaoAcademiaRepository,
            GeocodificacaoService geocodificacaoService,
            FotoAcademiaService fotoAcademiaService) {
        this.academiaRepository = academiaRepository;
        this.gerenteRepository = gerenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.categoriaAcademiaRepository = categoriaAcademiaRepository;
        this.facilidadeRepository = facilidadeRepository;
        this.facilidadeAcademiaRepository = facilidadeAcademiaRepository;
        this.fotoAcademiaRepository = fotoAcademiaRepository;
        this.itemAvaliacaoRepository = itemAvaliacaoRepository;
        this.itemAvaliacaoAcademiaRepository = itemAvaliacaoAcademiaRepository;
        this.geocodificacaoService = geocodificacaoService;
        this.fotoAcademiaService = fotoAcademiaService;
    }

    public List<Academia> findAllAtivas() {
        return carregarCategoriasVinculadas(academiaRepository.findByStatusAcademia("ATIVO"));
    }

    @Transactional(readOnly = true)
    public PaginaAcademiasDTO findAtivasParaHome(
            int page,
            String search,
            List<Long> categoriaIds,
            List<Long> facilidadeIds) {
        Page<Academia> academias = academiaRepository.findAtivasParaHome(
                normalizarBusca(search),
                normalizarIds(categoriaIds),
                normalizarIds(facilidadeIds),
                PageRequest.of(page, TAMANHO_PAGINA_HOME));

        List<Academia> content = academias.getContent().stream()
                .map(this::carregarCategoriasVinculadas)
                .toList();

        return new PaginaAcademiasDTO(
                content,
                academias.getNumber(),
                academias.getSize(),
                academias.getTotalElements(),
                academias.getTotalPages());
    }

    public List<Academia> findAll() {
        return carregarCategoriasVinculadas(academiaRepository.findAll());
    }

    public List<Academia> findByGerenteId(Long gerenteId, Authentication authentication) {
        validarGerenteSolicitado(gerenteId, authentication);

        return carregarCategoriasVinculadas(academiaRepository.findByGerenteId(gerenteId));
    }

    public List<Academia> findAtivasByGerenteId(Long gerenteId) {
        return carregarCategoriasVinculadas(academiaRepository.findByGerenteIdAndStatusAcademia(gerenteId, "ATIVO"));
    }

    public Academia findById(Long id) {
        Academia academia = academiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Academia nao encontrada."));

        return carregarCategoriasVinculadas(academia);
    }

    public List<AcademiaProximaDTO> findProximasPorUsuario(Authentication authentication) {
        return buscarAcademiasProximas(buscarUsuarioComumElegivel(authentication));
    }

    public List<AcademiaProximaDTO> findProximasPorUsuario(Long usuarioId, Authentication authentication) {
        Usuario usuario = buscarUsuarioComumElegivel(authentication);

        if (!usuario.getId().equals(usuarioId)) {
            throw acessoNegado();
        }

        return buscarAcademiasProximas(usuario);
    }

    @Transactional(readOnly = true)
    public List<AcademiaComparacaoDTO> comparar(List<Long> ids, Authentication authentication) {
        Usuario usuario = buscarUsuarioComumElegivel(authentication);
        validarIdsParaComparacao(ids);

        List<Academia> academias = ids.stream()
                .map(this::buscarAcademiaAtivaParaComparacao)
                .toList();
        List<Long> academiaIds = academias.stream().map(Academia::getId).toList();
        Map<Long, Map<Long, BigDecimal>> mediasPorAcademiaEItem = buscarMediasPorCriterio(academiaIds);
        List<AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO> criterios = itemAvaliacaoRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(item -> new AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO(
                        item.getId(), item.getNome(), item.getDescricao()))
                .toList();

        return academias.stream()
                .map(academia -> montarComparacaoAcademia(
                        academia,
                        usuario,
                        criterios,
                        mediasPorAcademiaEItem.getOrDefault(academia.getId(), Map.of())))
                .toList();
    }

    @Transactional
    public Academia create(Academia academia, Authentication authentication) {
        Gerente gerente = buscarGerenteElegivel(authentication);

        if (!DocumentoValidator.isValidCNPJ(academia.getCnpj())) {
            throw new RuntimeException("CNPJ invalido.");
        }

        academia.setCnpj(academia.getCnpj().replaceAll("\\D", ""));
        academia.setCep(limparCep(academia.getCep()));
        CoordenadasDTO coordenadas = geocodificarAcademia(academia, academia.getCep());
        academia.setLatitude(coordenadas.latitude());
        academia.setLongitude(coordenadas.longitude());

        academia.setGerente(gerente);
        academia.setDataCadastro(LocalDateTime.now());
        academia.setStatusAcademia("ATIVO");
        academia.setStatusAnteriorBloqueioGerente(null);

        // Academia nova ainda nao possui avaliacoes.
        // Por isso a nota deve comecar como NULL no banco.
        academia.setNota(null);

        List<Long> categoriaIds = academia.getCategoriaIds();
        List<Categoria> categoriasSelecionadas = buscarCategoriasAtivas(categoriaIds);
        List<Long> facilidadeIds = academia.getFacilidadeIds();
        List<Facilidade> facilidadesSelecionadas = buscarFacilidadesAtivas(facilidadeIds);

        if (categoriaIds != null) {
            academia.setCategorias(montarTextoCategorias(categoriasSelecionadas));
        }

        if (facilidadeIds != null) {
            academia.setFacilidades(montarTextoFacilidades(facilidadesSelecionadas));
        }

        Academia academiaSalva = academiaRepository.save(academia);
        sincronizarCategorias(academiaSalva, categoriasSelecionadas, categoriaIds != null);
        sincronizarFacilidades(academiaSalva, facilidadesSelecionadas, facilidadeIds != null);

        return carregarCategoriasVinculadas(academiaSalva);
    }

    @Transactional
    public Academia createComFotos(
            Academia academia,
            List<MultipartFile> fotos,
            Integer fotoPrincipalIndex,
            Authentication authentication) {
        Academia academiaSalva = create(academia, authentication);

        if (fotos == null || fotos.isEmpty()) {
            if (fotoPrincipalIndex != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Indice da foto principal invalido.");
            }
            return academiaSalva;
        }

        fotoAcademiaService.salvarLote(
                academiaSalva.getId(), fotos, fotoPrincipalIndex, authentication);

        return carregarCategoriasVinculadas(academiaSalva);
    }

    @Transactional
    public Academia update(Long id, Academia dadosAtualizados, Authentication authentication) {
        Academia academia = findById(id);
        validarPropriedadeAcademia(academia, authentication);

        if (!DocumentoValidator.isValidCNPJ(dadosAtualizados.getCnpj())) {
            throw new RuntimeException("CNPJ invalido.");
        }

        dadosAtualizados.setCnpj(dadosAtualizados.getCnpj().replaceAll("\\D", ""));
        String cepAtualizado = limparCep(dadosAtualizados.getCep());
        CoordenadasDTO novasCoordenadas = null;

        if (enderecoRelevanteAlterado(academia, dadosAtualizados, cepAtualizado)) {
            novasCoordenadas = geocodificarAcademia(dadosAtualizados, cepAtualizado);
        }

        academia.setNome(dadosAtualizados.getNome());
        academia.setCnpj(dadosAtualizados.getCnpj());
        academia.setDescricao(dadosAtualizados.getDescricao());
        academia.setCep(cepAtualizado);
        academia.setEndereco(dadosAtualizados.getEndereco());
        academia.setNumero(dadosAtualizados.getNumero());
        academia.setComplemento(dadosAtualizados.getComplemento());
        academia.setBairro(dadosAtualizados.getBairro());
        academia.setCidade(dadosAtualizados.getCidade());
        academia.setEstado(dadosAtualizados.getEstado());
        academia.setTelefone(dadosAtualizados.getTelefone());
        academia.setCelular(dadosAtualizados.getCelular());
        academia.setEmail(dadosAtualizados.getEmail());
        if (novasCoordenadas != null) {
            academia.setLatitude(novasCoordenadas.latitude());
            academia.setLongitude(novasCoordenadas.longitude());
        }
        academia.setCategorias(dadosAtualizados.getCategorias());
        academia.setFacilidades(dadosAtualizados.getFacilidades());

        // Nao atualiza a nota aqui.
        // A nota deve ser controlada pelo fluxo de avaliacoes.

        List<Long> categoriaIds = dadosAtualizados.getCategoriaIds();
        List<Categoria> categoriasSelecionadas = buscarCategoriasAtivas(categoriaIds);
        List<Long> facilidadeIds = dadosAtualizados.getFacilidadeIds();
        List<Facilidade> facilidadesSelecionadas = buscarFacilidadesAtivas(facilidadeIds);

        if (categoriaIds != null) {
            academia.setCategorias(montarTextoCategorias(categoriasSelecionadas));
        }

        if (facilidadeIds != null) {
            academia.setFacilidades(montarTextoFacilidades(facilidadesSelecionadas));
        }

        Academia academiaSalva = academiaRepository.save(academia);
        sincronizarCategorias(academiaSalva, categoriasSelecionadas, categoriaIds != null);
        sincronizarFacilidades(academiaSalva, facilidadesSelecionadas, facilidadeIds != null);

        return carregarCategoriasVinculadas(academiaSalva);
    }

    /**
     * Fluxo do gerente: quando o proprio gerente inativa a academia.
     */
    public Academia inativar(Long id, Authentication authentication) {
        Academia academia = findById(id);
        validarPropriedadeAcademia(academia, authentication);

        if ("SUSPENSA".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Esta academia foi suspensa pela administracao. Entre em contato com o suporte para reativar.");
        }

        academia.setStatusAcademia("INATIVO");

        return academiaRepository.save(academia);
    }

    /**
     * Fluxo do gerente: gerente so pode reativar academias INATIVAS.
     * Academia SUSPENSA so pode ser reativada pelo ADMIN.
     */
    public Academia reativar(Long id, Authentication authentication) {
        Academia academia = findById(id);
        validarPropriedadeAcademia(academia, authentication);

        if ("SUSPENSA".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Esta academia foi suspensa pela administracao. Entre em contato com o suporte para reativar.");
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

    private void validarGerenteSolicitado(Long gerenteId, Authentication authentication) {
        Gerente gerenteAutenticado = buscarGerenteElegivel(authentication);

        if (!gerenteAutenticado.getId().equals(gerenteId)) {
            throw acessoNegado();
        }
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

    private Usuario buscarUsuarioComumElegivel(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw acessoNegado();
        }

        Usuario usuario = usuarioRepository.findByUsername(normalizarUsername(authentication.getName()))
                .orElseThrow(this::acessoNegado);

        if (!"USER".equals(usuario.getNivelAcesso()) || !"ATIVO".equals(usuario.getStatusUsuario())) {
            throw acessoNegado();
        }

        return usuario;
    }

    private List<AcademiaProximaDTO> buscarAcademiasProximas(Usuario usuario) {
        if (usuario.getLatitude() == null || usuario.getLongitude() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuario nao possui coordenadas para localizar academias proximas.");
        }

        return academiaRepository.findByStatusAcademia("ATIVO")
                .stream()
                .filter(this::possuiCoordenadas)
                .map(academia -> new AcademiaComDistancia(
                        academia,
                        calcularDistanciaKm(usuario, academia)))
                .filter(academia -> academia.distanciaKm() <= RAIO_PADRAO_KM)
                .sorted(Comparator
                        .comparingDouble(AcademiaComDistancia::distanciaKm)
                        .thenComparing(academia -> academia.academia().getId()))
                .map(academia -> new AcademiaProximaDTO(
                        carregarCategoriasVinculadas(academia.academia()),
                        BigDecimal.valueOf(academia.distanciaKm()).setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private void validarIdsParaComparacao(List<Long> ids) {
        if (ids == null || ids.size() < 2 || ids.size() > 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe entre 2 e 3 academias para comparar.");
        }

        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IDs de academia invalidos.");
        }

        if (new LinkedHashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IDs de academia nao podem ser duplicados.");
        }
    }

    private Academia buscarAcademiaAtivaParaComparacao(Long academiaId) {
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Academia nao encontrada ou inativa para comparacao."));

        if (!"ATIVO".equals(academia.getStatusAcademia())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Academia nao encontrada ou inativa para comparacao.");
        }

        return academia;
    }

    private Map<Long, Map<Long, BigDecimal>> buscarMediasPorCriterio(List<Long> academiaIds) {
        Map<Long, Map<Long, BigDecimal>> medias = new LinkedHashMap<>();

        itemAvaliacaoAcademiaRepository.calcularMediasAtivasPorAcademiaIds(academiaIds)
                .forEach(resultado -> {
                    Long academiaId = ((Number) resultado[0]).longValue();
                    Long itemId = ((Number) resultado[1]).longValue();
                    BigDecimal media = BigDecimal.valueOf(((Number) resultado[2]).doubleValue())
                            .setScale(1, RoundingMode.HALF_UP);

                    medias.computeIfAbsent(academiaId, chave -> new LinkedHashMap<>())
                            .put(itemId, media);
                });

        return medias;
    }

    private AcademiaComparacaoDTO montarComparacaoAcademia(
            Academia academia,
            Usuario usuario,
            List<AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO> criterios,
            Map<Long, BigDecimal> mediasPorItem) {
        return new AcademiaComparacaoDTO(
                academia.getId(),
                academia.getNome(),
                buscarFotoPrincipal(academia.getId()),
                academia.getNota(),
                calcularDistanciaParaComparacao(usuario, academia),
                academia.getEndereco(),
                academia.getNumero(),
                academia.getComplemento(),
                academia.getBairro(),
                academia.getCidade(),
                academia.getEstado(),
                buscarCategoriasAtivasDaAcademia(academia.getId()),
                buscarFacilidadesAtivasDaAcademia(academia.getId()),
                criterios.stream()
                        .map(criterio -> new AcademiaComparacaoDTO.CriterioComparacaoDTO(
                                criterio.id(), criterio.nome(), mediasPorItem.get(criterio.id())))
                        .toList());
    }

    private AcademiaComparacaoDTO.FotoPrincipalComparacaoDTO buscarFotoPrincipal(Long academiaId) {
        return fotoAcademiaRepository
                .findByAcademiaIdAndStatusFotoAndPrincipalTrue(academiaId, "ATIVO")
                .map(foto -> new AcademiaComparacaoDTO.FotoPrincipalComparacaoDTO(
                        foto.getId(),
                        foto.getTipoArquivo(),
                        "/fotos-academia/" + foto.getId() + "/imagem"))
                .orElse(null);
    }

    private List<AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO> buscarCategoriasAtivasDaAcademia(
            Long academiaId) {
        return categoriaAcademiaRepository
                .findByAcademiaIdAndStatusCategoriaAcademia(academiaId, "ATIVO")
                .stream()
                .map(CategoriaAcademia::getCategoria)
                .filter(categoria -> "ATIVO".equals(categoria.getStatusCategoria()))
                .map(categoria -> new AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO(
                        categoria.getId(), categoria.getNome(), categoria.getDescricao()))
                .toList();
    }

    private List<AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO> buscarFacilidadesAtivasDaAcademia(
            Long academiaId) {
        return facilidadeAcademiaRepository
                .findByAcademiaIdAndStatusFacilidadeAcademia(academiaId, "ATIVO")
                .stream()
                .map(FacilidadeAcademia::getFacilidade)
                .filter(facilidade -> "ATIVO".equals(facilidade.getStatusFacilidade()))
                .map(facilidade -> new AcademiaComparacaoDTO.ItemEstruturadoComparacaoDTO(
                        facilidade.getId(), facilidade.getNome(), facilidade.getDescricao()))
                .toList();
    }

    private BigDecimal calcularDistanciaParaComparacao(Usuario usuario, Academia academia) {
        if (!possuiCoordenadasValidas(usuario.getLatitude(), usuario.getLongitude())
                || !possuiCoordenadasValidas(academia.getLatitude(), academia.getLongitude())) {
            return null;
        }

        return BigDecimal.valueOf(calcularDistanciaKm(usuario, academia))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean possuiCoordenadasValidas(BigDecimal latitude, BigDecimal longitude) {
        return latitude != null && longitude != null
                && latitude.compareTo(BigDecimal.valueOf(-90)) >= 0
                && latitude.compareTo(BigDecimal.valueOf(90)) <= 0
                && longitude.compareTo(BigDecimal.valueOf(-180)) >= 0
                && longitude.compareTo(BigDecimal.valueOf(180)) <= 0;
    }

    private boolean possuiCoordenadas(Academia academia) {
        return academia.getLatitude() != null && academia.getLongitude() != null;
    }

    private double calcularDistanciaKm(Usuario usuario, Academia academia) {
        double latitudeUsuarioEmRadianos = Math.toRadians(usuario.getLatitude().doubleValue());
        double latitudeAcademiaEmRadianos = Math.toRadians(academia.getLatitude().doubleValue());
        double diferencaLatitude = latitudeAcademiaEmRadianos - latitudeUsuarioEmRadianos;
        double diferencaLongitude = Math.toRadians(
                academia.getLongitude().doubleValue() - usuario.getLongitude().doubleValue());

        double senoLatitude = Math.sin(diferencaLatitude / 2);
        double senoLongitude = Math.sin(diferencaLongitude / 2);
        double haversine = senoLatitude * senoLatitude
                + Math.cos(latitudeUsuarioEmRadianos) * Math.cos(latitudeAcademiaEmRadianos)
                        * senoLongitude * senoLongitude;

        return 2 * RAIO_TERRA_KM * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private List<Academia> carregarCategoriasVinculadas(List<Academia> academias) {
        academias.forEach(this::carregarCategoriasVinculadas);
        return academias;
    }

    private Academia carregarCategoriasVinculadas(Academia academia) {
        List<Categoria> categorias = categoriaAcademiaRepository
                .findByAcademiaIdAndStatusCategoriaAcademia(academia.getId(), "ATIVO")
                .stream()
                .map(CategoriaAcademia::getCategoria)
                .toList();

        academia.setCategoriasVinculadas(categorias);
        academia.setCategoriaIds(categorias.stream().map(Categoria::getId).toList());
        carregarFacilidadesVinculadas(academia);
        carregarFotoPrincipal(academia);

        return academia;
    }

    private String normalizarBusca(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    private List<Long> normalizarIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Academia carregarFacilidadesVinculadas(Academia academia) {
        List<Facilidade> facilidades = facilidadeAcademiaRepository
                .findByAcademiaIdAndStatusFacilidadeAcademia(academia.getId(), "ATIVO")
                .stream()
                .map(FacilidadeAcademia::getFacilidade)
                .toList();

        academia.setFacilidadesVinculadas(facilidades);
        academia.setFacilidadeIds(facilidades.stream().map(Facilidade::getId).toList());

        return academia;
    }

    private Academia carregarFotoPrincipal(Academia academia) {
        FotoAcademiaDTO fotoPrincipal = fotoAcademiaRepository
                .findByAcademiaIdAndStatusFotoAndPrincipalTrue(academia.getId(), "ATIVO")
                .map(foto -> new FotoAcademiaDTO(
                        foto.getId(),
                        academia.getId(),
                        foto.getTipoArquivo(),
                        foto.getDataCadastro(),
                        foto.isPrincipal()))
                .orElse(null);

        academia.setFotoPrincipal(fotoPrincipal);
        return academia;
    }

    private List<Categoria> buscarCategoriasAtivas(List<Long> categoriaIds) {
        if (categoriaIds == null) {
            return List.of();
        }

        Set<Long> idsUnicos = new LinkedHashSet<>(categoriaIds);
        List<Categoria> categorias = new ArrayList<>();

        for (Long categoriaId : idsUnicos) {
            if (categoriaId == null) {
                continue;
            }

            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoria nao encontrada."));

            if (!"ATIVO".equals(categoria.getStatusCategoria())) {
                throw new RuntimeException("Categoria inativa nao pode ser vinculada a uma academia.");
            }

            categorias.add(categoria);
        }

        return categorias;
    }

    private List<Facilidade> buscarFacilidadesAtivas(List<Long> facilidadeIds) {
        if (facilidadeIds == null) {
            return List.of();
        }

        Set<Long> idsUnicos = new LinkedHashSet<>(facilidadeIds);
        List<Facilidade> facilidades = new ArrayList<>();

        for (Long facilidadeId : idsUnicos) {
            if (facilidadeId == null) {
                continue;
            }

            Facilidade facilidade = facilidadeRepository.findById(facilidadeId)
                    .orElseThrow(() -> new RuntimeException("Facilidade nao encontrada."));

            if (!"ATIVO".equals(facilidade.getStatusFacilidade())) {
                throw new RuntimeException("Facilidade inativa nao pode ser vinculada a uma academia.");
            }

            facilidades.add(facilidade);
        }

        return facilidades;
    }

    private void sincronizarCategorias(
            Academia academia,
            List<Categoria> categoriasSelecionadas,
            boolean deveSincronizar) {

        if (!deveSincronizar) {
            return;
        }

        Set<Long> idsSelecionados = categoriasSelecionadas.stream()
                .map(Categoria::getId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<CategoriaAcademia> vinculosAtuais = categoriaAcademiaRepository.findByAcademiaId(academia.getId());
        Map<Long, CategoriaAcademia> vinculoPorCategoriaId = new LinkedHashMap<>();
        List<CategoriaAcademia> vinculosParaSalvar = new ArrayList<>();

        for (CategoriaAcademia vinculo : vinculosAtuais) {
            Long categoriaId = vinculo.getCategoria().getId();

            if (!vinculoPorCategoriaId.containsKey(categoriaId)) {
                vinculoPorCategoriaId.put(categoriaId, vinculo);
                continue;
            }

            if ("ATIVO".equals(vinculo.getStatusCategoriaAcademia())) {
                vinculo.setStatusCategoriaAcademia("INATIVO");
                vinculosParaSalvar.add(vinculo);
            }
        }

        for (Categoria categoria : categoriasSelecionadas) {
            CategoriaAcademia vinculo = vinculoPorCategoriaId.get(categoria.getId());

            if (vinculo == null) {
                vinculo = new CategoriaAcademia();
                vinculo.setAcademia(academia);
                vinculo.setCategoria(categoria);
            }

            if (!"ATIVO".equals(vinculo.getStatusCategoriaAcademia())) {
                vinculo.setStatusCategoriaAcademia("ATIVO");
                vinculosParaSalvar.add(vinculo);
            }
        }

        for (CategoriaAcademia vinculo : vinculoPorCategoriaId.values()) {
            Long categoriaId = vinculo.getCategoria().getId();

            if (!idsSelecionados.contains(categoriaId)
                    && "ATIVO".equals(vinculo.getStatusCategoriaAcademia())) {
                vinculo.setStatusCategoriaAcademia("INATIVO");
                vinculosParaSalvar.add(vinculo);
            }
        }

        if (!vinculosParaSalvar.isEmpty()) {
            categoriaAcademiaRepository.saveAll(vinculosParaSalvar);
        }
    }

    private void sincronizarFacilidades(
            Academia academia,
            List<Facilidade> facilidadesSelecionadas,
            boolean deveSincronizar) {

        if (!deveSincronizar) {
            return;
        }

        Set<Long> idsSelecionados = facilidadesSelecionadas.stream()
                .map(Facilidade::getId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<FacilidadeAcademia> vinculosAtuais = facilidadeAcademiaRepository.findByAcademiaId(academia.getId());
        Map<Long, FacilidadeAcademia> vinculoPorFacilidadeId = new LinkedHashMap<>();
        List<FacilidadeAcademia> vinculosParaSalvar = new ArrayList<>();

        for (FacilidadeAcademia vinculo : vinculosAtuais) {
            Long facilidadeId = vinculo.getFacilidade().getId();

            if (!vinculoPorFacilidadeId.containsKey(facilidadeId)) {
                vinculoPorFacilidadeId.put(facilidadeId, vinculo);
                continue;
            }

            if ("ATIVO".equals(vinculo.getStatusFacilidadeAcademia())) {
                vinculo.setStatusFacilidadeAcademia("INATIVO");
                vinculosParaSalvar.add(vinculo);
            }
        }

        for (Facilidade facilidade : facilidadesSelecionadas) {
            FacilidadeAcademia vinculo = vinculoPorFacilidadeId.get(facilidade.getId());

            if (vinculo == null) {
                vinculo = new FacilidadeAcademia();
                vinculo.setAcademia(academia);
                vinculo.setFacilidade(facilidade);
            }

            if (!"ATIVO".equals(vinculo.getStatusFacilidadeAcademia())) {
                vinculo.setStatusFacilidadeAcademia("ATIVO");
                vinculosParaSalvar.add(vinculo);
            }
        }

        for (FacilidadeAcademia vinculo : vinculoPorFacilidadeId.values()) {
            Long facilidadeId = vinculo.getFacilidade().getId();

            if (!idsSelecionados.contains(facilidadeId)
                    && "ATIVO".equals(vinculo.getStatusFacilidadeAcademia())) {
                vinculo.setStatusFacilidadeAcademia("INATIVO");
                vinculosParaSalvar.add(vinculo);
            }
        }

        if (!vinculosParaSalvar.isEmpty()) {
            facilidadeAcademiaRepository.saveAll(vinculosParaSalvar);
        }
    }

    private String montarTextoCategorias(List<Categoria> categorias) {
        return String.join(", ", categorias.stream().map(Categoria::getNome).toList());
    }

    private String montarTextoFacilidades(List<Facilidade> facilidades) {
        return String.join(", ", facilidades.stream().map(Facilidade::getNome).toList());
    }

    private CoordenadasDTO geocodificarAcademia(Academia academia, String cep) {
        validarEnderecoParaGeocodificacao(academia);

        return geocodificacaoService.geocodificar(new EnderecoGeocodificacaoDTO(
                academia.getEndereco(),
                academia.getNumero(),
                academia.getBairro(),
                academia.getCidade(),
                academia.getEstado(),
                cep,
                academia.getComplemento()));
    }

    private void validarEnderecoParaGeocodificacao(Academia academia) {
        if (!textoPreenchido(academia.getEndereco())
                || academia.getNumero() == null
                || !textoPreenchido(academia.getCidade())
                || !textoPreenchido(academia.getEstado())) {
            throw new GeocodificacaoException("Endereco insuficiente para geocodificacao.");
        }
    }

    private boolean enderecoRelevanteAlterado(
            Academia academia,
            Academia dadosAtualizados,
            String cepAtualizado) {
        return !Objects.equals(limparCep(academia.getCep()), cepAtualizado)
                || !Objects.equals(normalizarTexto(academia.getEndereco()), normalizarTexto(dadosAtualizados.getEndereco()))
                || numerosDiferentes(academia.getNumero(), dadosAtualizados.getNumero())
                || !Objects.equals(normalizarTexto(academia.getBairro()), normalizarTexto(dadosAtualizados.getBairro()))
                || !Objects.equals(normalizarTexto(academia.getCidade()), normalizarTexto(dadosAtualizados.getCidade()))
                || !Objects.equals(normalizarTexto(academia.getEstado()), normalizarTexto(dadosAtualizados.getEstado()));
    }

    private boolean numerosDiferentes(BigDecimal primeiroNumero, BigDecimal segundoNumero) {
        if (primeiroNumero == null || segundoNumero == null) {
            return primeiroNumero != segundoNumero;
        }

        return primeiroNumero.compareTo(segundoNumero) != 0;
    }

    private boolean textoPreenchido(String texto) {
        return normalizarTexto(texto) != null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String textoNormalizado = texto.trim().replaceAll("\\s+", " ");
        return textoNormalizado.isBlank() ? null : textoNormalizado.toLowerCase(Locale.ROOT);
    }

    private String limparCep(String cep) {
        if (cep == null) {
            return "";
        }

        return cep.replaceAll("\\D", "");
    }

    private record AcademiaComDistancia(Academia academia, double distanciaKm) {
    }
}
