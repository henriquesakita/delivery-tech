package com.deliverytech.delivery_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestauranteRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;
    
    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos numéricos")
    private String cnpj;
    
    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 200, message = "Endereço não pode exceder 200 caracteres")
    private String endereco;
    
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter entre 10 e 11 dígitos")
    private String telefone;
    
    @NotBlank(message = "Especialidade é obrigatória")
    @Size(max = 50, message = "Especialidade não pode exceder 50 caracteres")
    private String especialidade;
    
    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    private String descricao;
    
    @NotBlank(message = "Horário de funcionamento é obrigatório")
    @Size(max = 100, message = "Horário de funcionamento não pode exceder 100 caracteres")
    private String horarioFuncionamento;
}
