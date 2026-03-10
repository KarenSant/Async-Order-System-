package com.async.order.system;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRabbit
public class AsyncOrderSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncOrderSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner testarRabbit(RabbitTemplate rabbitTemplate) {
        return args -> {
            try {
                System.out.println(">>> Enviando mensagem de teste para o RabbitMQ...");
                rabbitTemplate.convertAndSend("pedidos.entrada.local", "Hello RabbitMQ - Teste de Conexão!");
            } catch (Exception e) {
                System.err.println(">>> ERRO AO CONECTAR NO RABBITMQ: " + e.getMessage());
            }
        };
    }

    @RabbitListener(queues = "pedidos.entrada.local")
    public void escutarTeste(String mensagem) {
        System.out.println(">>> MENSAGEM RECEBIDA COM SUCESSO: " + mensagem);
    }
}
