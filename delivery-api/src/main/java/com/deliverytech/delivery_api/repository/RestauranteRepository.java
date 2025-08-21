package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    
    // Busca restaurante pelo CNPJ
    Optional<Restaurante> findByCnpj(String cnpj);
    
    // Verifica se existe restaurante com o CNPJ informado
    boolean existsByCnpj(String cnpj);
    
    // Busca restaurantes ativos
    List<Restaurante> findByAtivoTrue();
    
    // Busca restaurantes por especialidade
    List<Restaurante> findByEspecialidadeContainingIgnoreCase(String especialidade);
    
    // Busca restaurantes por nome
    List<Restaurante> findByNomeContainingIgnoreCase(String nome);
    
    // Busca restaurantes com produtos disponíveis
    @Query("SELECT DISTINCT r FROM Restaurante r JOIN r.produtos p WHERE p.disponivel = true AND r.ativo = true")
    List<Restaurante> findRestaurantesWithProdutosDisponiveis();
    
    // Ranking de restaurantes por número de pedidos
    @Query(value = "SELECT r.nome, COUNT(p.id) as total_pedidos " +
                  "FROM restaurante r " +
                  "LEFT JOIN pedido p ON r.id = p.restaurante_id " +
                  "WHERE r.ativo = true " +
                  "GROUP BY r.id, r.nome " +
                  "ORDER BY total_pedidos DESC " +
                  "LIMIT 10", nativeQuery = true)
    List<Object[]> rankingRestaurantesPorPedidos();
}
