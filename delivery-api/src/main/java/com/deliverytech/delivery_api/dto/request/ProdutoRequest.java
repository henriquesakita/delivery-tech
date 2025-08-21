package com.deliverytech.delivery_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;
    
    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    private String descricao;
    
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal preco;
    
    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 50, message = "Categoria não pode exceder 50 caracteres")
    private String categoria;
    
    private String imagemUrl;
    
    private Boolean disponivel;
    
    // ID do restaurante associado ao produto
    @NotNull(message = "ID do restaurante é obrigatório")
    private Long restauranteId;
}
