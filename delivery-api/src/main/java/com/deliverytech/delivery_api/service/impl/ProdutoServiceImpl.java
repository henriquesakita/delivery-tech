package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.ProdutoRequest;
import com.deliverytech.delivery_api.model.Produto;
import com.deliverytech.delivery_api.model.Restaurante;
import com.deliverytech.delivery_api.repository.ProdutoRepository;
import com.deliverytech.delivery_api.service.ProdutoService;
import com.deliverytech.delivery_api.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final RestauranteService restauranteService;

    /**
     * Cadastra um novo produto
     */
    @Override
    public Produto cadastrar(ProdutoRequest produtoRequest) {
        log.info("Iniciando cadastro de produto: {}", produtoRequest.getNome());
        
        // Buscar o restaurante pelo ID
        Restaurante restaurante = restauranteService.buscarPorId(produtoRequest.getRestauranteId())
            .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + produtoRequest.getRestauranteId()));

        // Converter ProdutoRequest para Produto
        Produto produto = new Produto();
        produto.setNome(produtoRequest.getNome());
        produto.setDescricao(produtoRequest.getDescricao());
        produto.setPreco(produtoRequest.getPreco());
        produto.setCategoria(produtoRequest.getCategoria());
        produto.setImagemUrl(produtoRequest.getImagemUrl());
        produto.setDisponivel(produtoRequest.getDisponivel() != null ? produtoRequest.getDisponivel() : true);
        produto.setRestaurante(restaurante);
        
        // Validações de negócio
        validarDadosProduto(produto);
        
        Produto produtoSalvo = produtoRepository.save(produto);
        log.info("Produto cadastrado com sucesso - ID: {}", produtoSalvo.getId());
        
        return produtoSalvo;
    }

    /**
     * Busca produto por ID
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long id) {
        log.info("Buscando produto por ID: {}", id);
        return produtoRepository.findById(id);
    }

    /**
     * Lista produtos por restaurante
     */
    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarPorRestaurante(Long restauranteId) {
        log.info("Listando produtos do restaurante ID: {}", restauranteId);
        // Verificar se restaurante existe
        if (!restauranteService.buscarPorId(restauranteId).isPresent()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + restauranteId);
        }
        return produtoRepository.findByRestauranteId(restauranteId);
    }

    /**
     * Lista produtos disponíveis por restaurante
     */
    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarDisponiveisPorRestaurante(Long restauranteId) {
        log.info("Listando produtos disponíveis do restaurante ID: {}", restauranteId);
        // Verificar se restaurante existe
        if (!restauranteService.buscarPorId(restauranteId).isPresent()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + restauranteId);
        }
        return produtoRepository.findByRestauranteIdAndDisponivelTrue(restauranteId);
    }

    /**
     * Atualiza dados do produto
     */
    @Override
    public Produto atualizar(Long id, ProdutoRequest produtoRequest) {
        log.info("Atualizando produto ID: {}", id);
        
        // Buscar o produto pelo ID
        Produto produto = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        
        // Verificar se o restaurante existe
        Restaurante restaurante = restauranteService.buscarPorId(produtoRequest.getRestauranteId())
            .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + produtoRequest.getRestauranteId()));
        
        // Verificar se o produto pertence ao restaurante informado
        if (!produto.getRestaurante().getId().equals(restaurante.getId())) {
            throw new IllegalArgumentException("O produto não pertence ao restaurante informado");
        }

        // Atualizar dados do produto
        produto.setNome(produtoRequest.getNome());
        produto.setDescricao(produtoRequest.getDescricao());
        produto.setPreco(produtoRequest.getPreco());
        produto.setCategoria(produtoRequest.getCategoria());
        produto.setImagemUrl(produtoRequest.getImagemUrl());
        
        if (produtoRequest.getDisponivel() != null) {
            produto.setDisponivel(produtoRequest.getDisponivel());
        }
        
        // Validar dados atualizados
        validarDadosProduto(produto);
        
        Produto produtoSalvo = produtoRepository.save(produto);
        log.info("Produto atualizado com sucesso - ID: {}", produtoSalvo.getId());
        
        return produtoSalvo;
    }

    /**
     * Exclui produto (fisicamente do banco)
     */
    @Override
    public void excluir(Long id) {
        log.info("Excluindo produto ID: {}", id);
        
        Produto produto = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        
        try {
            produtoRepository.delete(produto);
            log.info("Produto excluído com sucesso - ID: {}", id);
        } catch (Exception e) {
            log.error("Erro ao excluir produto - ID: {}", id, e);
            throw new IllegalStateException("Não foi possível excluir o produto. Ele pode estar associado a pedidos.");
        }
    }

    /**
     * Alterna disponibilidade do produto
     */
    @Override
    public Produto alterarDisponibilidade(Long id) {
        log.info("Alterando disponibilidade do produto ID: {}", id);
        
        Produto produto = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        
        produto.setDisponivel(!produto.getDisponivel());
        Produto produtoSalvo = produtoRepository.save(produto);
        
        String status = produtoSalvo.getDisponivel() ? "disponibilizado" : "indisponibilizado";
        log.info("Produto {} com sucesso - ID: {}", status, id);
        
        return produtoSalvo;
    }

    /**
     * Busca produtos por nome e restaurante
     */
    @Override
    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(Long restauranteId, String nome) {
        log.info("Buscando produtos por nome: {} no restaurante ID: {}", nome, restauranteId);
        
        // Verificar se restaurante existe
        if (!restauranteService.buscarPorId(restauranteId).isPresent()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + restauranteId);
        }
        
        return produtoRepository.findByRestauranteIdAndNomeContainingIgnoreCase(restauranteId, nome);
    }

    /**
     * Busca produtos por categoria e restaurante
     */
    @Override
    @Transactional(readOnly = true)
    public List<Produto> buscarPorCategoria(Long restauranteId, String categoria) {
        log.info("Buscando produtos por categoria: {} no restaurante ID: {}", categoria, restauranteId);
        
        // Verificar se restaurante existe
        if (!restauranteService.buscarPorId(restauranteId).isPresent()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + restauranteId);
        }
        
        return produtoRepository.findByRestauranteIdAndCategoriaContainingIgnoreCase(restauranteId, categoria);
    }

    /**
     * Busca produtos por faixa de preço e restaurante
     */
    @Override
    @Transactional(readOnly = true)
    public List<Produto> buscarPorFaixaDePreco(Long restauranteId, BigDecimal precoMin, BigDecimal precoMax) {
        log.info("Buscando produtos por faixa de preço: {} - {} no restaurante ID: {}", 
                precoMin, precoMax, restauranteId);
        
        // Verificar se restaurante existe
        if (!restauranteService.buscarPorId(restauranteId).isPresent()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + restauranteId);
        }
        
        if (precoMin == null) {
            precoMin = BigDecimal.ZERO;
        }
        
        if (precoMax == null) {
            precoMax = new BigDecimal("999999.99");
        }
        
        if (precoMin.compareTo(precoMax) > 0) {
            throw new IllegalArgumentException("Preço mínimo não pode ser maior que o preço máximo");
        }
        
        return produtoRepository.findByRestauranteIdAndPrecoBetween(restauranteId, precoMin, precoMax);
    }

    /**
     * Produtos mais vendidos por restaurante
     */
    @Override
    @Transactional(readOnly = true)
    public List<Produto> produtosMaisVendidos(Long restauranteId) {
        log.info("Buscando produtos mais vendidos do restaurante ID: {}", restauranteId);
        
        // Verificar se restaurante existe
        if (!restauranteService.buscarPorId(restauranteId).isPresent()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + restauranteId);
        }
        
        return produtoRepository.findMostSoldProductsByRestaurante(restauranteId);
    }

    /**
     * Validações de negócio para produtos
     */
    private void validarDadosProduto(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (produto.getNome().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres");
        }

        if (produto.getNome().length() > 100) {
            throw new IllegalArgumentException("Nome não pode ter mais de 100 caracteres");
        }

        if (produto.getPreco() == null) {
            throw new IllegalArgumentException("Preço é obrigatório");
        }

        if (produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser maior que zero");
        }

        if (produto.getCategoria() == null || produto.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }

        if (produto.getRestaurante() == null) {
            throw new IllegalArgumentException("Restaurante é obrigatório");
        }
    }
}
