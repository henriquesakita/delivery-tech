package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    // Buscar produtos por restaurante
    List<Produto> findByRestauranteId(Long restauranteId);
    
    // Buscar produtos disponíveis por restaurante
    List<Produto> findByRestauranteIdAndDisponivelTrue(Long restauranteId);
    
    // Buscar produtos por categoria e restaurante
    List<Produto> findByRestauranteIdAndCategoriaContainingIgnoreCase(Long restauranteId, String categoria);
    
    // Buscar produtos por nome e restaurante
    List<Produto> findByRestauranteIdAndNomeContainingIgnoreCase(Long restauranteId, String nome);
    
    // Buscar produtos por faixa de preço e restaurante
    List<Produto> findByRestauranteIdAndPrecoBetween(Long restauranteId, BigDecimal precoMin, BigDecimal precoMax);
    
    // Buscar produtos mais vendidos
    @Query(value = "SELECT p.*, COUNT(ip.id) as vendas " +
                   "FROM produto p " +
                   "JOIN item_pedido ip ON p.id = ip.produto_id " +
                   "JOIN pedido ped ON ip.pedido_id = ped.id " +
                   "WHERE p.restaurante_id = :restauranteId " +
                   "GROUP BY p.id " +
                   "ORDER BY vendas DESC " +
                   "LIMIT 10", nativeQuery = true)
    List<Produto> findMostSoldProductsByRestaurante(@Param("restauranteId") Long restauranteId);
}
