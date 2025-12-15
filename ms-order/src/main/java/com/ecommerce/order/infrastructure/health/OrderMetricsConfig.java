package com.ecommerce.order.infrastructure.health;

import com.ecommerce.order.domain.repository.OrderRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderMetricsConfig {

    public OrderMetricsConfig(MeterRegistry meterRegistry, OrderRepository orderRepository) {
        
        // Gauge pour le nombre total de commandes
        Gauge.builder("orders.total", orderRepository, repo -> repo.count())
                .description("Nombre total de commandes dans le système")
                .register(meterRegistry);
        
        // Gauge pour les commandes en attente
        Gauge.builder("orders.pending", orderRepository, 
                repo -> repo.findByStatus(com.ecommerce.order.domain.enumerate.OrderStatus.PENDING).size())
                .description("Nombre de commandes en attente")
                .register(meterRegistry);
        
        // Gauge pour les commandes confirmées
        Gauge.builder("orders.confirmed", orderRepository, 
                repo -> repo.findByStatus(com.ecommerce.order.domain.enumerate.OrderStatus.CONFIRMED).size())
                .description("Nombre de commandes confirmées")
                .register(meterRegistry);
        
        // Gauge pour les commandes expédiées
        Gauge.builder("orders.shipped", orderRepository, 
                repo -> repo.findByStatus(com.ecommerce.order.domain.enumerate.OrderStatus.SHIPPED).size())
                .description("Nombre de commandes expédiées")
                .register(meterRegistry);
        
        // Gauge pour les commandes livrées
        Gauge.builder("orders.delivered", orderRepository, 
                repo -> repo.findByStatus(com.ecommerce.order.domain.enumerate.OrderStatus.DELIVERED).size())
                .description("Nombre de commandes livrées")
                .register(meterRegistry);
    }
}
