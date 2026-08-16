package br.itb.projeto.logym.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.logym.model.entity.Academia;
import br.itb.projeto.logym.model.entity.Facilidade;
import br.itb.projeto.logym.model.entity.FacilidadeAcademia;
import br.itb.projeto.logym.repository.AcademiaRepository;
import br.itb.projeto.logym.repository.FacilidadeAcademiaRepository;
import br.itb.projeto.logym.repository.FacilidadeRepository;

@Service
public class FacilidadeService {

    private final FacilidadeRepository facilidadeRepository;
    private final FacilidadeAcademiaRepository facilidadeAcademiaRepository;
    private final AcademiaRepository academiaRepository;

    public FacilidadeService(
            FacilidadeRepository facilidadeRepository,
            FacilidadeAcademiaRepository facilidadeAcademiaRepository,
            AcademiaRepository academiaRepository) {
        this.facilidadeRepository = facilidadeRepository;
        this.facilidadeAcademiaRepository = facilidadeAcademiaRepository;
        this.academiaRepository = academiaRepository;
    }

    public List<Facilidade> findAllAtivas() {
        return facilidadeRepository.findByStatusFacilidadeOrderByNomeAsc("ATIVO");
    }

    public List<Facilidade> findAll() {
        return facilidadeRepository.findAll();
    }

    public Facilidade findById(Long id) {
        return facilidadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facilidade nao encontrada."));
    }

    @Transactional
    public Facilidade create(Facilidade facilidade) {
        validarNomeObrigatorio(facilidade.getNome());

        String nome = facilidade.getNome().trim();

        if (facilidadeRepository.findByNomeIgnoreCase(nome).isPresent()) {
            throw new RuntimeException("Ja existe uma facilidade cadastrada com este nome.");
        }

        Facilidade novaFacilidade = new Facilidade();
        novaFacilidade.setNome(nome);
        novaFacilidade.setDescricao(normalizarDescricao(facilidade.getDescricao()));
        novaFacilidade.setStatusFacilidade("ATIVO");

        return facilidadeRepository.save(novaFacilidade);
    }

    @Transactional
    public Facilidade update(Long id, Facilidade dadosAtualizados) {
        validarNomeObrigatorio(dadosAtualizados.getNome());

        Facilidade facilidade = findById(id);
        String nome = dadosAtualizados.getNome().trim();

        facilidadeRepository.findByNomeIgnoreCase(nome)
                .filter(facilidadeExistente -> !facilidadeExistente.getId().equals(id))
                .ifPresent(facilidadeExistente -> {
                    throw new RuntimeException("Ja existe uma facilidade cadastrada com este nome.");
                });

        facilidade.setNome(nome);
        facilidade.setDescricao(normalizarDescricao(dadosAtualizados.getDescricao()));

        Facilidade facilidadeSalva = facilidadeRepository.save(facilidade);
        recalcularFacilidadesTextoDasAcademiasVinculadas(facilidadeSalva.getId());

        return facilidadeSalva;
    }

    @Transactional
    public Facilidade inativar(Long id) {
        Facilidade facilidade = findById(id);
        facilidade.setStatusFacilidade("INATIVO");

        Facilidade facilidadeSalva = facilidadeRepository.save(facilidade);
        recalcularFacilidadesTextoDasAcademiasVinculadas(facilidadeSalva.getId());

        return facilidadeSalva;
    }

    @Transactional
    public Facilidade reativar(Long id) {
        Facilidade facilidade = findById(id);
        facilidade.setStatusFacilidade("ATIVO");

        Facilidade facilidadeSalva = facilidadeRepository.save(facilidade);
        recalcularFacilidadesTextoDasAcademiasVinculadas(facilidadeSalva.getId());

        return facilidadeSalva;
    }

    private void validarNomeObrigatorio(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("O nome da facilidade e obrigatorio.");
        }
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return null;
        }

        return descricao.trim();
    }

    private void recalcularFacilidadesTextoDasAcademiasVinculadas(Long facilidadeId) {
        Map<Long, Academia> academiasAfetadas = new LinkedHashMap<>();

        for (FacilidadeAcademia vinculo : facilidadeAcademiaRepository.findByFacilidadeId(facilidadeId)) {
            Academia academia = vinculo.getAcademia();

            if (academia != null && academia.getId() != null) {
                academiasAfetadas.putIfAbsent(academia.getId(), academia);
            }
        }

        if (academiasAfetadas.isEmpty()) {
            return;
        }

        for (Academia academia : academiasAfetadas.values()) {
            academia.setFacilidades(montarTextoFacilidadesAtivas(academia.getId()));
        }

        academiaRepository.saveAll(academiasAfetadas.values());
    }

    private String montarTextoFacilidadesAtivas(Long academiaId) {
        List<String> nomes = facilidadeAcademiaRepository
                .findByAcademiaIdAndStatusFacilidadeAcademia(academiaId, "ATIVO")
                .stream()
                .map(FacilidadeAcademia::getFacilidade)
                .filter(facilidade -> "ATIVO".equals(facilidade.getStatusFacilidade()))
                .map(Facilidade::getNome)
                .toList();

        return String.join(", ", nomes);
    }
}
