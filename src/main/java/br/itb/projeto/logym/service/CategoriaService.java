package br.itb.projeto.logym.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.logym.model.entity.Categoria;
import br.itb.projeto.logym.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> findAllAtivas() {
        return categoriaRepository.findByStatusCategoriaOrderByNomeAsc("ATIVO");
    }

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public Categoria findById(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria nao encontrada."));
    }

    @Transactional
    public Categoria create(Categoria categoria) {
        validarNomeObrigatorio(categoria.getNome());

        String nome = categoria.getNome().trim();

        if (categoriaRepository.findByNomeIgnoreCase(nome).isPresent()) {
            throw new RuntimeException("Ja existe uma categoria cadastrada com este nome.");
        }

        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome(nome);
        novaCategoria.setDescricao(normalizarDescricao(categoria.getDescricao()));
        novaCategoria.setStatusCategoria("ATIVO");

        return categoriaRepository.save(novaCategoria);
    }

    @Transactional
    public Categoria update(Long id, Categoria dadosAtualizados) {
        validarNomeObrigatorio(dadosAtualizados.getNome());

        Categoria categoria = findById(id);
        String nome = dadosAtualizados.getNome().trim();

        categoriaRepository.findByNomeIgnoreCase(nome)
                .filter(categoriaExistente -> !categoriaExistente.getId().equals(id))
                .ifPresent(categoriaExistente -> {
                    throw new RuntimeException("Ja existe uma categoria cadastrada com este nome.");
                });

        categoria.setNome(nome);
        categoria.setDescricao(normalizarDescricao(dadosAtualizados.getDescricao()));

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria inativar(Long id) {
        Categoria categoria = findById(id);
        categoria.setStatusCategoria("INATIVO");

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria reativar(Long id) {
        Categoria categoria = findById(id);
        categoria.setStatusCategoria("ATIVO");

        return categoriaRepository.save(categoria);
    }

    private void validarNomeObrigatorio(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("O nome da categoria e obrigatorio.");
        }
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return null;
        }

        return descricao.trim();
    }
}
