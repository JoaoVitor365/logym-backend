package br.itb.projeto.logym.service;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Categoria;
import br.itb.projeto.logym.model.entity.CategoriaAcademia;
import br.itb.projeto.logym.model.entity.Gerente;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.CategoriaAcademiaRepository;
import br.itb.projeto.logym.repository.CategoriaRepository;
import br.itb.projeto.logym.repository.GerenteRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;
import br.itb.projeto.logym.util.DocumentoValidator;

@Service
public class AcademiaService {

    private static final BigDecimal LATITUDE_MINIMA = new BigDecimal("-90");
    private static final BigDecimal LATITUDE_MAXIMA = new BigDecimal("90");
    private static final BigDecimal LONGITUDE_MINIMA = new BigDecimal("-180");
    private static final BigDecimal LONGITUDE_MAXIMA = new BigDecimal("180");

    private final AcademiaRepository academiaRepository;
    private final GerenteRepository gerenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaAcademiaRepository categoriaAcademiaRepository;

    public AcademiaService(
            AcademiaRepository academiaRepository,
            GerenteRepository gerenteRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            CategoriaAcademiaRepository categoriaAcademiaRepository) {
        this.academiaRepository = academiaRepository;
        this.gerenteRepository = gerenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.categoriaAcademiaRepository = categoriaAcademiaRepository;
    }

    public List<Academia> findAllAtivas() {
        return carregarCategoriasVinculadas(academiaRepository.findByStatusAcademia("ATIVO"));
    }

    public List<Academia> findAll() {
        return carregarCategoriasVinculadas(academiaRepository.findAll());
    }

    public List<Academia> findByGerenteId(Long gerenteId) {
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

    public List<Academia> findProximasPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado."));

        String cepUsuario = limparCep(usuario.getCep());

        List<Academia> academias = academiaRepository.findByStatusAcademia("ATIVO");

        academias.sort(
                Comparator
                        .comparingInt(
                                (Academia academia) -> calcularPontuacaoCep(cepUsuario, limparCep(academia.getCep())))
                        .reversed());

        return carregarCategoriasVinculadas(academias);
    }

    @Transactional
    public Academia create(Academia academia) {

        if (!DocumentoValidator.isValidCNPJ(academia.getCnpj())) {
            throw new RuntimeException("CNPJ invalido.");
        }

        academia.setCnpj(academia.getCnpj().replaceAll("\\D", ""));
        academia.setCep(limparCep(academia.getCep()));
        validarCoordenadas(academia.getLatitude(), academia.getLongitude());

        if (academia.getGerente() == null || academia.getGerente().getId() == null) {
            throw new RuntimeException("Gerente nao informado.");
        }

        Gerente gerente = gerenteRepository.findById(academia.getGerente().getId())
                .orElseThrow(() -> new RuntimeException("Gerente nao encontrado."));

        academia.setGerente(gerente);
        academia.setDataCadastro(LocalDateTime.now());
        academia.setStatusAcademia("ATIVO");

        // Academia nova ainda nao possui avaliacoes.
        // Por isso a nota deve comecar como NULL no banco.
        academia.setNota(null);

        List<Long> categoriaIds = academia.getCategoriaIds();
        List<Categoria> categoriasSelecionadas = buscarCategoriasAtivas(categoriaIds);

        if (categoriaIds != null) {
            academia.setCategorias(montarTextoCategorias(categoriasSelecionadas));
        }

        Academia academiaSalva = academiaRepository.save(academia);
        sincronizarCategorias(academiaSalva, categoriasSelecionadas, categoriaIds != null);

        return carregarCategoriasVinculadas(academiaSalva);
    }

    @Transactional
    public Academia update(Long id, Academia dadosAtualizados) {

        if (!DocumentoValidator.isValidCNPJ(dadosAtualizados.getCnpj())) {
            throw new RuntimeException("CNPJ invalido.");
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
        atualizarCoordenadas(academia, dadosAtualizados);
        academia.setCategorias(dadosAtualizados.getCategorias());
        academia.setFacilidades(dadosAtualizados.getFacilidades());

        // Nao atualiza a nota aqui.
        // A nota deve ser controlada pelo fluxo de avaliacoes.

        List<Long> categoriaIds = dadosAtualizados.getCategoriaIds();
        List<Categoria> categoriasSelecionadas = buscarCategoriasAtivas(categoriaIds);

        if (categoriaIds != null) {
            academia.setCategorias(montarTextoCategorias(categoriasSelecionadas));
        }

        Academia academiaSalva = academiaRepository.save(academia);
        sincronizarCategorias(academiaSalva, categoriasSelecionadas, categoriaIds != null);

        return carregarCategoriasVinculadas(academiaSalva);
    }

    /**
     * Fluxo do gerente: quando o proprio gerente inativa a academia.
     */
    public Academia inativar(Long id) {
        Academia academia = findById(id);

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
    public Academia reativar(Long id) {
        Academia academia = findById(id);

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

    private String montarTextoCategorias(List<Categoria> categorias) {
        return String.join(", ", categorias.stream().map(Categoria::getNome).toList());
    }

    private void atualizarCoordenadas(Academia academia, Academia dadosAtualizados) {
        BigDecimal latitude = dadosAtualizados.getLatitude();
        BigDecimal longitude = dadosAtualizados.getLongitude();

        if (latitude == null && longitude == null) {
            return;
        }

        validarCoordenadas(latitude, longitude);
        academia.setLatitude(latitude);
        academia.setLongitude(longitude);
    }

    private void validarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null && longitude == null) {
            return;
        }

        if (latitude == null || longitude == null) {
            throw new RuntimeException("Latitude e longitude devem ser informadas juntas.");
        }

        if (latitude.compareTo(LATITUDE_MINIMA) < 0 || latitude.compareTo(LATITUDE_MAXIMA) > 0) {
            throw new RuntimeException("Latitude invalida. Informe um valor entre -90 e 90.");
        }

        if (longitude.compareTo(LONGITUDE_MINIMA) < 0 || longitude.compareTo(LONGITUDE_MAXIMA) > 0) {
            throw new RuntimeException("Longitude invalida. Informe um valor entre -180 e 180.");
        }
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
