package com.async.order.system.infrastructure.in;

import com.async.order.system.infrastructure.RabbitConfig;
import com.async.order.system.model.Pedido;
import com.async.order.system.service.PedidoService;
import com.async.order.system.service.PedidoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PedidoListener {
    private final PedidoService pedidoService;
    private final RabbitTemplate rabbitTemplate;
    private final PedidoStatusService statusService;

    @RabbitListener(queues = RabbitConfig.QUEUE_ENTRADA)
    public void onMessage(Map<String, Object> mensagem) {
        UUID id = null;
        try {
            id = UUID.fromString(mensagem.get("id").toString());
            int quantidade = Integer.parseInt(mensagem.get("quantidade").toString());
            String produto = (String) mensagem.get("produto");

            Pedido pedido = new Pedido(id, produto, quantidade, LocalDateTime.now());

            pedidoService.processar(pedido);

            statusService.atualizarStatus(id, "SUCESSO");
            Map<String, Object> statusSucesso = Map.of("idPedido", id, "status", "SUCESSO", "dataProcessamento", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_STATUS_SUCESSO, statusSucesso);

            System.out.println(">>> [SUCESSO] Pedido processado: " + id);

        } catch (Exception e) {
            String erroMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";

            if (id != null) {
                statusService.atualizarStatus(id, "FALHA");
                // Falha: Publica na fila de FALHA para o requisito
                Map<String, Object> statusFalha = Map.of("idPedido", id, "status", "FALHA", "mensagemErro", erroMsg);
                rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_STATUS_FALHA, statusFalha);
            }

            System.err.println(">>> [ERRO] Rejeitando mensagem para DLQ: " + erroMsg);

            // Rejeita para o RabbitMQ mover para a Dead Letter Queue
            throw new AmqpRejectAndDontRequeueException(erroMsg);
        }
    }
}