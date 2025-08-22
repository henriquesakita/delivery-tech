package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.model.Produto;
import com.deliverytech.delivery_api.service.ProdutoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Implementação didática de ProdutoService.
 * Ajuste os nomes de repositórios e propriedades conforme seu projeto.
 */
@Service
@Transactional(readOnly = true)
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository,
                              RestauranteRepository restauranteRepository) {
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    // === OPERAÇÕES BÁSICAS ===
    @Override
    @Transactional
    public Produto cadastrar(Produto produto) {
        validarPreco(produto.getPreco());

        if (produto.getRestaurante() == null || produto.getRestaurante().getId() == null) {
            throw new IllegalArgumentException("Produto deve pertencer a um restaurante válido.");
        }

        // Garante que o restaurante existe
        var restauranteId = produto.getRestaurante().getId();
        restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new NoSuchElementException("Restaurante não encontrado: " + restauranteId));

        // Disponível por padrão (ajuste conforme regra)
        if (produto.getDisponivel() == null) {
            produto.setDisponivel(true);
        }

        return produtoRepository.save(produto);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Override
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Override
    @Transactional
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        var existente = produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: " + id));

        if (produtoAtualizado.getNome() != null) existente.setNome(produtoAtualizado.getNome());
        if (produtoAtualizado.getDescricao() != null) existente.setDescricao(produtoAtualizado.getDescricao());
        if (produtoAtualizado.getCategoria() != null) existente.setCategoria(produtoAtualizado.getCategoria());
        if (produtoAtualizado.getPreco() != null) {
            validarPreco(produtoAtualizado.getPreco());
            existente.setPreco(produtoAtualizado.getPreco());
        }
        if (produtoAtualizado.getDisponivel() != null) existente.setDisponivel(produtoAtualizado.getDisponivel());

        return produtoRepository.save(existente);
    }

    @Override
    @Transactional
    public void inativar(Long id) {
        var existente = produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: " + id));
        existente.setDisponivel(false);
        produtoRepository.save(existente);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new NoSuchElementException("Produto não encontrado: " + id);
        }
        produtoRepository.deleteById(id);
    }

    // === BUSCAS ESPECÍFICAS ===
    @Override
    public List<Produto> buscarPorRestaurante(Long restauranteId) {
        return produtoRepository.findByRestauranteId(restauranteId);
    }

    @Override
    public List<Produto> buscarPorCategoria(String categoria) {
        return produtoRepository.findByCategoriaIgnoreCase(categoria);
    }

    @Override
    public List<Produto> listarDisponiveis() {
        return produtoRepository.findByDisponivelTrue();
    }

    @Override
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    // === REGRAS DE NEGÓCIO ===
    @Override
    @Transactional
    public void alterarDisponibilidade(Long id, boolean disponivel) {
        var existente = produtoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produto não encontrado: " + id));
        existente.setDisponivel(disponivel);
        produtoRepository.save(existente);
    }

    @Override
    public void validarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero.");
        }
    }

    // Repositórios didáticos (substitua pelos reais do projeto)
    public interface ProdutoRepository {
        Produto save(Produto p);
        Optional<Produto> findById(Long id);
        boolean existsById(Long id);
        void deleteById(Long id);
        List<Produto> findAll();
        List<Produto> findByRestauranteId(Long restauranteId);
        List<Produto> findByCategoriaIgnoreCase(String categoria);
        List<Produto> findByDisponivelTrue();
        List<Produto> findByNomeContainingIgnoreCase(String nome);
    }

    public interface RestauranteRepository {
        Optional<Object> findById(Long id);
    }
}
