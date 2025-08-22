package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.model.Pedido;
import com.deliverytech.delivery_api.model.Produto;
import com.deliverytech.delivery_api.service.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Override
    @Transactional
    public Pedido criar(Pedido pedido) {
        // Exemplo: valida itens e calcula total inicial
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter itens.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (var item : pedido.getItens()) {
            Produto p = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new NoSuchElementException("Produto inexistente no item."));
            if (Boolean.FALSE.equals(p.getDisponivel())) {
                throw new IllegalStateException("Produto indisponível: " + p.getNome());
            }
            BigDecimal subtotal = p.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);
            // opcional: copiar preço atual para o item, evitando variação posterior
            item.setPrecoUnitario(p.getPreco());
        }
        pedido.setTotal(total);
        pedido.setStatus(StatusPedido.CRIADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Override
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Override
    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Override
    @Transactional
    public Pedido atualizarStatus(Long id, StatusPedido novoStatus) {
        var pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado: " + id));

        // Regras simples de transição de status (exemplo)
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Pedido cancelado não pode mudar de status.");
        }
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void cancelar(Long id, String motivo) {
        var pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado: " + id));
        if (pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw new IllegalStateException("Pedido entregue não pode ser cancelado.");
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setMotivoCancelamento(motivo);
        pedidoRepository.save(pedido);
    }

    @Override
    public BigDecimal calcularTotal(Long id) {
        var pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado: " + id));
        return pedido.getTotal();
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new NoSuchElementException("Pedido não encontrado: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    // Tipos auxiliares didáticos
    public enum StatusPedido { CRIADO, EM_PREPARO, SAIU_PARA_ENTREGA, ENTREGUE, CANCELADO }

    public interface PedidoRepository {
        Pedido save(Pedido p);
        Optional<Pedido> findById(Long id);
        boolean existsById(Long id);
        void deleteById(Long id);
        List<Pedido> findAll();
        List<Pedido> findByClienteId(Long clienteId);
    }

    public interface ProdutoRepository {
        Optional<Produto> findById(Long id);
    }
}
