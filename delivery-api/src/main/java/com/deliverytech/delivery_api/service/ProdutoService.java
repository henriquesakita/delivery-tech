package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.ProdutoRequest;
import com.deliverytech.delivery_api.model.Produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    
    // Operações básicas
    Produto cadastrar(ProdutoRequest produtoRequest);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> listarPorRestaurante(Long restauranteId);
    List<Produto> listarDisponiveisPorRestaurante(Long restauranteId);
    Produto atualizar(Long id, ProdutoRequest produtoRequest);
    void excluir(Long id);
    
    // Operações de disponibilidade
    Produto alterarDisponibilidade(Long id);
    
    // Consultas especializadas
    List<Produto> buscarPorNome(Long restauranteId, String nome);
    List<Produto> buscarPorCategoria(Long restauranteId, String categoria);
    List<Produto> buscarPorFaixaDePreco(Long restauranteId, BigDecimal precoMin, BigDecimal precoMax);
    List<Produto> produtosMaisVendidos(Long restauranteId);
}
