package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.RestauranteRequest;
import com.deliverytech.delivery_api.model.Restaurante;
import com.deliverytech.delivery_api.repository.RestauranteRepository;
import com.deliverytech.delivery_api.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RestauranteServiceImpl implements RestauranteService {

    private final RestauranteRepository restauranteRepository;

    /**
     * Cadastra um novo restaurante
     */
    @Override
    public Restaurante cadastrar(RestauranteRequest restauranteRequest) {
        log.info("Iniciando cadastro de restaurante: {}", restauranteRequest.getNome());
        
        // Converter RestauranteRequest para Restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setNome(restauranteRequest.getNome());
        restaurante.setCnpj(restauranteRequest.getCnpj());
        restaurante.setEndereco(restauranteRequest.getEndereco());
        restaurante.setTelefone(restauranteRequest.getTelefone());
        restaurante.setEspecialidade(restauranteRequest.getEspecialidade());
        restaurante.setDescricao(restauranteRequest.getDescricao());
        restaurante.setHorarioFuncionamento(restauranteRequest.getHorarioFuncionamento());
        
        // Validar CNPJ único
        if (restauranteRepository.existsByCnpj(restaurante.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado: " + restaurante.getCnpj());
        }
        
        // Validações de negócio
        validarDadosRestaurante(restaurante);
        
        // Definir como ativo por padrão
        restaurante.setAtivo(true);
        
        Restaurante restauranteSalvo = restauranteRepository.save(restaurante);
        log.info("Restaurante cadastrado com sucesso - ID: {}", restauranteSalvo.getId());
        
        return restauranteSalvo;
    }

    /**
     * Busca restaurante por ID
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Restaurante> buscarPorId(Long id) {
        log.info("Buscando restaurante por ID: {}", id);
        return restauranteRepository.findById(id);
    }

    /**
     * Busca restaurante por CNPJ
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Restaurante> buscarPorCnpj(String cnpj) {
        log.info("Buscando restaurante por CNPJ: {}", cnpj);
        return restauranteRepository.findByCnpj(cnpj);
    }

    /**
     * Lista todos os restaurantes ativos
     */
    @Override
    @Transactional(readOnly = true)
    public List<Restaurante> listarAtivos() {
        log.info("Listando todos os restaurantes ativos");
        return restauranteRepository.findByAtivoTrue();
    }

    /**
     * Busca restaurantes por nome
     */
    @Override
    @Transactional(readOnly = true)
    public List<Restaurante> buscarPorNome(String nome) {
        log.info("Buscando restaurantes por nome: {}", nome);
        return restauranteRepository.findByNomeContainingIgnoreCase(nome);
    }

    /**
     * Busca restaurantes por especialidade
     */
    @Override
    @Transactional(readOnly = true)
    public List<Restaurante> buscarPorEspecialidade(String especialidade) {
        log.info("Buscando restaurantes por especialidade: {}", especialidade);
        return restauranteRepository.findByEspecialidadeContainingIgnoreCase(especialidade);
    }

    /**
     * Atualiza dados do restaurante
     */
    @Override
    public Restaurante atualizar(Long id, RestauranteRequest restauranteRequest) {
        log.info("Atualizando restaurante ID: {}", id);
        
        Restaurante restaurante = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));

        // Verificar se CNPJ não está sendo usado por outro restaurante
        if (!restaurante.getCnpj().equals(restauranteRequest.getCnpj()) && 
            restauranteRepository.existsByCnpj(restauranteRequest.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado: " + restauranteRequest.getCnpj());
        }

        // Criar restaurante temporário para validação
        Restaurante restauranteParaValidacao = new Restaurante();
        restauranteParaValidacao.setNome(restauranteRequest.getNome());
        restauranteParaValidacao.setCnpj(restauranteRequest.getCnpj());
        restauranteParaValidacao.setEndereco(restauranteRequest.getEndereco());
        restauranteParaValidacao.setTelefone(restauranteRequest.getTelefone());
        restauranteParaValidacao.setEspecialidade(restauranteRequest.getEspecialidade());
        restauranteParaValidacao.setDescricao(restauranteRequest.getDescricao());
        restauranteParaValidacao.setHorarioFuncionamento(restauranteRequest.getHorarioFuncionamento());

        // Validar dados atualizados
        validarDadosRestaurante(restauranteParaValidacao);

        // Atualizar campos
        restaurante.setNome(restauranteRequest.getNome());
        restaurante.setCnpj(restauranteRequest.getCnpj());
        restaurante.setEndereco(restauranteRequest.getEndereco());
        restaurante.setTelefone(restauranteRequest.getTelefone());
        restaurante.setEspecialidade(restauranteRequest.getEspecialidade());
        restaurante.setDescricao(restauranteRequest.getDescricao());
        restaurante.setHorarioFuncionamento(restauranteRequest.getHorarioFuncionamento());
        
        Restaurante restauranteSalvo = restauranteRepository.save(restaurante);
        log.info("Restaurante atualizado com sucesso - ID: {}", restauranteSalvo.getId());
        
        return restauranteSalvo;
    }

    /**
     * Inativa restaurante (soft delete)
     */
    @Override
    public void inativar(Long id) {
        log.info("Inativando restaurante ID: {}", id);
        
        Restaurante restaurante = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));
        
        restaurante.inativar();
        restauranteRepository.save(restaurante);
        
        log.info("Restaurante inativado com sucesso - ID: {}", id);
    }

    /**
     * Ativa/Desativa restaurante (toggle status)
     */
    @Override
    public Restaurante ativarDesativarRestaurante(Long id) {
        log.info("Alterando status do restaurante ID: {}", id);
        
        Restaurante restaurante = buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));
        
        restaurante.setAtivo(!restaurante.getAtivo());
        Restaurante restauranteSalvo = restauranteRepository.save(restaurante);
        
        String statusAtual = restauranteSalvo.getAtivo() ? "ativado" : "desativado";
        log.info("Restaurante {} com sucesso - ID: {}", statusAtual, id);
        
        return restauranteSalvo;
    }

    /**
     * Busca restaurantes com produtos disponíveis
     */
    @Override
    @Transactional(readOnly = true)
    public List<Restaurante> buscarComProdutosDisponiveis() {
        log.info("Buscando restaurantes com produtos disponíveis");
        return restauranteRepository.findRestaurantesWithProdutosDisponiveis();
    }

    /**
     * Ranking de restaurantes por número de pedidos
     */
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> rankingPorPedidos() {
        log.info("Gerando ranking de restaurantes por pedidos");
        return restauranteRepository.rankingRestaurantesPorPedidos();
    }

    /**
     * Validações de negócio
     */
    private void validarDadosRestaurante(Restaurante restaurante) {
        if (restaurante.getNome() == null || restaurante.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (restaurante.getCnpj() == null || restaurante.getCnpj().trim().isEmpty()) {
            throw new IllegalArgumentException("CNPJ é obrigatório");
        }

        if (restaurante.getNome().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres");
        }

        if (restaurante.getNome().length() > 100) {
            throw new IllegalArgumentException("Nome não pode ter mais de 100 caracteres");
        }

        if (restaurante.getCnpj().length() != 14 || !restaurante.getCnpj().matches("\\d+")) {
            throw new IllegalArgumentException("CNPJ deve ter 14 dígitos numéricos");
        }

        if (restaurante.getEndereco() == null || restaurante.getEndereco().trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço é obrigatório");
        }

        if (restaurante.getTelefone() == null || restaurante.getTelefone().trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }

        if (!restaurante.getTelefone().matches("\\d{10,11}")) {
            throw new IllegalArgumentException("Telefone deve ter entre 10 e 11 dígitos");
        }

        if (restaurante.getEspecialidade() == null || restaurante.getEspecialidade().trim().isEmpty()) {
            throw new IllegalArgumentException("Especialidade é obrigatória");
        }

        if (restaurante.getHorarioFuncionamento() == null || restaurante.getHorarioFuncionamento().trim().isEmpty()) {
            throw new IllegalArgumentException("Horário de funcionamento é obrigatório");
        }
    }
}
