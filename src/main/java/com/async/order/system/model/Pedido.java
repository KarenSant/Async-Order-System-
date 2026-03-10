package com.async.order.system.model;

import java.util.UUID;
import java.time.LocalDateTime;

public record Pedido(UUID id, String produto, int quantidade, LocalDateTime dataCriacao) {

}

