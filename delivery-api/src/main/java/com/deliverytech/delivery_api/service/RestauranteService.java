package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.RestauranteRequest;
import com.deliverytech.delivery_api.model.Restaurante;

import java.util.List;
import java.util.Optional;

public interface RestauranteService {
    
    // Operações básicas
    Restaurante cadastrar(RestauranteRequest restauranteRequest);
    Optional<Restaurante> buscarPorId(Long id);
    Optional<Restaurante> buscarPorCnpj(String cnpj);
    List<Restaurante> listarAtivos();
    List<Restaurante> buscarPorNome(String nome);
    List<Restaurante> buscarPorEspecialidade(String especialidade);
    Restaurante atualizar(Long id, RestauranteRequest restauranteRequest);
    void inativar(Long id);
    
    // Operação de toggle de status
    Restaurante ativarDesativarRestaurante(Long id);
    
    // Busca especializada
    List<Restaurante> buscarComProdutosDisponiveis();
    
    // Relatórios
    List<Object[]> rankingPorPedidos();
}
