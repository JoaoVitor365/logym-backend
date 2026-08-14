package br.itb.projeto.logym.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.logym.dto.AvaliacaoDTO;
import br.itb.projeto.logym.dto.AvaliacaoPorItensRequestDTO;
import br.itb.projeto.logym.dto.ItemAvaliacaoDTO;
import br.itb.projeto.logym.dto.ItemNotaAvaliacaoDTO;
import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Avaliacao;
import br.itb.projeto.logym.model.entity.ItemAvaliacao;
import br.itb.projeto.logym.model.entity.ItemAvaliacaoAcademia;
import br.itb.projeto.logym.model.entity.Usuario;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.AvaliacaoRepository;
import br.itb.projeto.logym.repository.ItemAvaliacaoAcademiaRepository;
import br.itb.projeto.logym.repository.ItemAvaliacaoRepository;
import br.itb.projeto.logym.repository.UsuarioRepository;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AcademiaRepository academiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemAvaliacaoRepository itemAvaliacaoRepository;
    private final ItemAvaliacaoAcademiaRepository itemAvaliacaoAcademiaRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            AcademiaRepository academiaRepository,
            UsuarioRepository usuarioRepository,
            ItemAvaliacaoRepository itemAvaliacaoRepository,
            ItemAvaliacaoAcademiaRepository itemAvaliacaoAcademiaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.academiaRepository = academiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.itemAvaliacaoRepository = itemAvaliacaoRepository;
        this.itemAvaliacaoAcademiaRepository = itemAvaliacaoAcademiaRepository;
    }

    public List<ItemAvaliacaoDTO> listarItensAvaliacao() {
        return itemAvaliacaoRepository.findAllByOrderByIdAsc()
                .stream()
                .map(item -> new ItemAvaliacaoDTO(
                        item.getId(),
                        item.getNome(),
                        item.getDescricao()))
                .toList();
    }

    @Transactional
    public AvaliacaoDTO avaliarPorItens(Long usuarioId, Long academiaId, AvaliacaoPorItensRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"USER".equals(usuario.getNivelAcesso())) {
            throw new RuntimeException("Apenas usuários comuns podem avaliar academias.");
        }

        if (!"ATIVO".equals(usuario.getStatusUsuario())) {
            throw new RuntimeException("Apenas usuários ativos podem avaliar academias.");
        }

        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada."));

        if ("SUSPENSA".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Esta academia foi suspensa pela administração e não pode receber avaliações.");
        }

        if (!"ATIVO".equals(academia.getStatusAcademia())) {
            throw new RuntimeException("Não é possível avaliar uma academia inativa.");
        }

        List<ItemAvaliacao> itensCadastrados = itemAvaliacaoRepository.findAllByOrderByIdAsc();

        if (itensCadastrados.isEmpty()) {
            throw new RuntimeException("Nenhum item de avaliação foi cadastrado.");
        }

        Map<Long, BigDecimal> notasPorItem = validarItensEnviados(request, itensCadastrados);
        BigDecimal mediaAvaliacao = calcularMedia(notasPorItem.values().stream().toList());

        LocalDateTime agora = LocalDateTime.now();

        Avaliacao avaliacao = avaliacaoRepository
                .findByUsuarioIdAndAcademiaId(usuarioId, academiaId)
                .orElse(null);

        if (avaliacao != null && "SUSPENSA".equals(avaliacao.getStatusAvaliacao())) {
            throw new RuntimeException("Sua avaliação foi suspensa pela administração. Entre em contato com o suporte para mais informações.");
        }

        if (avaliacao == null) {
            avaliacao = new Avaliacao();
            avaliacao.setUsuario(usuario);
            avaliacao.setAcademia(academia);
            avaliacao.setDataCadastro(agora);
            avaliacao.setComentario(null);
        }

        avaliacao.setNota(mediaAvaliacao);
        avaliacao.setDataAtualizacao(agora);
        avaliacao.setStatusAvaliacao("ATIVO");

        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);

        salvarOuAtualizarNotasDosItens(usuario, academia, itensCadastrados, notasPorItem);
        atualizarNotaMediaAcademia(academiaId);

        return toDTO(avaliacaoSalva);
    }

    /**
     * Lista avaliações públicas de uma academia.
     * Retorna avaliações ATIVAS para todos.
     * Se usuarioId for informado, inclui também a avaliação SUSPENSA daquele usuário,
     * para que ele veja o aviso de suspensão da própria avaliação.
     */
    public List<AvaliacaoDTO> findByAcademiaId(Long academiaId, Long usuarioId) {
        List<Avaliacao> avaliacoesAtivas = avaliacaoRepository
                .findByAcademiaIdAndStatusAvaliacaoOrderByDataCadastroDesc(academiaId, "ATIVO");

        List<Avaliacao> resultado = new ArrayList<>(avaliacoesAtivas);

        if (usuarioId != null) {
            avaliacaoRepository.findByUsuarioIdAndAcademiaId(usuarioId, academiaId)
                    .ifPresent(avaliacaoDoUsuario -> {
                        boolean jaIncluida = resultado.stream()
                                .anyMatch(avaliacao -> avaliacao.getId().equals(avaliacaoDoUsuario.getId()));

                        if (!jaIncluida && "SUSPENSA".equals(avaliacaoDoUsuario.getStatusAvaliacao())) {
                            resultado.add(0, avaliacaoDoUsuario);
                        }
                    });
        }

        return resultado.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AvaliacaoDTO> findAllAdmin() {
        return avaliacaoRepository.findAllByOrderByDataCadastroDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void inativar(Long avaliacaoId, Long usuarioId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));

        if (!avaliacao.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Você só pode remover sua própria avaliação.");
        }

        if ("SUSPENSA".equals(avaliacao.getStatusAvaliacao())) {
            throw new RuntimeException("Esta avaliação foi suspensa pela administração e não pode ser removida pelo usuário.");
        }

        avaliacao.setStatusAvaliacao("INATIVO");
        avaliacao.setDataAtualizacao(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);

        alterarStatusItensDaAvaliacao(avaliacao.getAcademia().getId(), avaliacao.getUsuario().getId(), false);
        atualizarNotaMediaAcademia(avaliacao.getAcademia().getId());
    }

    @Transactional
    public AvaliacaoDTO suspenderAdmin(Long avaliacaoId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));

        avaliacao.setStatusAvaliacao("SUSPENSA");
        avaliacao.setDataAtualizacao(LocalDateTime.now());
        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);

        alterarStatusItensDaAvaliacao(avaliacao.getAcademia().getId(), avaliacao.getUsuario().getId(), false);
        atualizarNotaMediaAcademia(avaliacao.getAcademia().getId());

        return toDTO(avaliacaoSalva);
    }

    @Transactional
    public AvaliacaoDTO reativarAdmin(Long avaliacaoId) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada."));

        if ("SUSPENSA".equals(avaliacao.getAcademia().getStatusAcademia())) {
            throw new RuntimeException("Não é possível reativar avaliação de uma academia suspensa.");
        }

        avaliacao.setStatusAvaliacao("ATIVO");
        avaliacao.setDataAtualizacao(LocalDateTime.now());
        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);

        alterarStatusItensDaAvaliacao(avaliacao.getAcademia().getId(), avaliacao.getUsuario().getId(), true);
        atualizarNotaMediaAcademia(avaliacao.getAcademia().getId());

        return toDTO(avaliacaoSalva);
    }

    private Map<Long, BigDecimal> validarItensEnviados(
            AvaliacaoPorItensRequestDTO request,
            List<ItemAvaliacao> itensCadastrados) {
        if (request == null || request.getItens() == null || request.getItens().isEmpty()) {
            throw new RuntimeException("Informe a nota de todos os itens da avaliação.");
        }

        Set<Long> idsItensCadastrados = itensCadastrados.stream()
                .map(ItemAvaliacao::getId)
                .collect(Collectors.toSet());

        Map<Long, BigDecimal> notasPorItem = new HashMap<>();
        Set<Long> idsRepetidos = new HashSet<>();

        for (ItemNotaAvaliacaoDTO itemRecebido : request.getItens()) {
            if (itemRecebido.getItemId() == null) {
                throw new RuntimeException("Todos os itens da avaliação devem informar o itemId.");
            }

            if (!idsItensCadastrados.contains(itemRecebido.getItemId())) {
                throw new RuntimeException("Item de avaliação inválido: " + itemRecebido.getItemId() + ".");
            }

            if (notasPorItem.containsKey(itemRecebido.getItemId())) {
                idsRepetidos.add(itemRecebido.getItemId());
            }

            validarNota(itemRecebido.getNota());
            notasPorItem.put(itemRecebido.getItemId(), itemRecebido.getNota());
        }

        if (!idsRepetidos.isEmpty()) {
            throw new RuntimeException("Não envie o mesmo item de avaliação mais de uma vez.");
        }

        if (!notasPorItem.keySet().containsAll(idsItensCadastrados) || notasPorItem.size() != idsItensCadastrados.size()) {
            throw new RuntimeException("Todos os itens de avaliação cadastrados devem receber uma nota.");
        }

        return notasPorItem;
    }

    private void salvarOuAtualizarNotasDosItens(
            Usuario usuario,
            Academia academia,
            List<ItemAvaliacao> itensCadastrados,
            Map<Long, BigDecimal> notasPorItem) {
        List<ItemAvaliacaoAcademia> itensParaSalvar = new ArrayList<>();

        for (ItemAvaliacao item : itensCadastrados) {
            ItemAvaliacaoAcademia itemAvaliacaoAcademia = itemAvaliacaoAcademiaRepository
                    .findByAcademiaIdAndUsuarioIdAndItemId(academia.getId(), usuario.getId(), item.getId())
                    .orElseGet(ItemAvaliacaoAcademia::new);

            itemAvaliacaoAcademia.setItem(item);
            itemAvaliacaoAcademia.setAcademia(academia);
            itemAvaliacaoAcademia.setUsuario(usuario);
            itemAvaliacaoAcademia.setNota(notasPorItem.get(item.getId()).setScale(1, RoundingMode.HALF_UP));
            itemAvaliacaoAcademia.setStatusAvaliacao(true);

            itensParaSalvar.add(itemAvaliacaoAcademia);
        }

        itemAvaliacaoAcademiaRepository.saveAll(itensParaSalvar);
    }

    private void alterarStatusItensDaAvaliacao(Long academiaId, Long usuarioId, boolean ativo) {
        List<ItemAvaliacaoAcademia> itens = itemAvaliacaoAcademiaRepository
                .findByAcademiaIdAndUsuarioIdOrderByItemIdAsc(academiaId, usuarioId);

        itens.forEach(item -> item.setStatusAvaliacao(ativo));
        itemAvaliacaoAcademiaRepository.saveAll(itens);
    }

    private BigDecimal calcularMedia(List<BigDecimal> notas) {
        BigDecimal soma = notas.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return soma.divide(BigDecimal.valueOf(notas.size()), 1, RoundingMode.HALF_UP);
    }

    private void validarNota(BigDecimal nota) {
        if (nota == null) {
            throw new RuntimeException("A nota é obrigatória em todos os itens.");
        }

        if (nota.compareTo(BigDecimal.ONE) < 0 || nota.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new RuntimeException("Cada nota deve estar entre 1 e 5.");
        }
    }

    private void atualizarNotaMediaAcademia(Long academiaId) {
        Academia academia = academiaRepository.findById(academiaId)
                .orElseThrow(() -> new RuntimeException("Academia não encontrada."));

        Double media = avaliacaoRepository.calcularMediaPorAcademiaId(academiaId);

        if (media == null) {
            academia.setNota(null);
        } else {
            academia.setNota(BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP));
        }

        academiaRepository.save(academia);
    }

    private AvaliacaoDTO toDTO(Avaliacao avaliacao) {
        List<ItemNotaAvaliacaoDTO> itens = itemAvaliacaoAcademiaRepository
                .findByAcademiaIdAndUsuarioIdOrderByItemIdAsc(
                        avaliacao.getAcademia().getId(),
                        avaliacao.getUsuario().getId())
                .stream()
                .map(this::toItemNotaDTO)
                .toList();

        return new AvaliacaoDTO(
                avaliacao.getId(),
                avaliacao.getComentario(),
                avaliacao.getNota(),
                avaliacao.getAcademia().getId(),
                avaliacao.getAcademia().getNome(),
                avaliacao.getUsuario().getId(),
                avaliacao.getUsuario().getNome(),
                avaliacao.getDataCadastro(),
                avaliacao.getDataAtualizacao(),
                avaliacao.getStatusAvaliacao(),
                itens);
    }

    private ItemNotaAvaliacaoDTO toItemNotaDTO(ItemAvaliacaoAcademia itemAvaliacaoAcademia) {
        return new ItemNotaAvaliacaoDTO(
                itemAvaliacaoAcademia.getItem().getId(),
                itemAvaliacaoAcademia.getItem().getNome(),
                itemAvaliacaoAcademia.getItem().getDescricao(),
                itemAvaliacaoAcademia.getNota(),
                itemAvaliacaoAcademia.getStatusAvaliacao());
    }
}
