package com.ecommerce.order.infrastructure.metrics;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.enumerate.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Configuration des métriques personnalisées pour le service Order
 * Expose des métriques métier pour Prometheus/Grafana
 */
@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final OrderRepository orderRepository;

    // Compteur de commandes créées
    private Counter ordersCreatedCounter;

    public MetricsConfig(MeterRegistry meterRegistry, OrderRepository orderRepository) {
        this.meterRegistry = meterRegistry;
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void initMetrics() {
        // Gauge: Nombre total de commandes
        Gauge.builder("orders_total", orderRepository, OrderRepository::count)
                .description("Nombre total de commandes")
                .register(meterRegistry);

        // Gauge: Commandes par statut PENDING
        Gauge.builder("orders_by_status", orderRepository, repo ->
                repo.findByStatus(OrderStatus.PENDING).size())
                .tag("status", "PENDING")
                .description("Nombre de commandes par statut")
                .register(meterRegistry);

        // Gauge: Commandes par statut CONFIRMED
        Gauge.builder("orders_by_status", orderRepository, repo ->
                repo.findByStatus(OrderStatus.CONFIRMED).size())
                .tag("status", "CONFIRMED")
                .description("Nombre de commandes par statut")
                .register(meterRegistry);

        // Gauge: Commandes par statut SHIPPED
        Gauge.builder("orders_by_status", orderRepository, repo ->
                repo.findByStatus(OrderStatus.SHIPPED).size())
                .tag("status", "SHIPPED")
                .description("Nombre de commandes par statut")
                .register(meterRegistry);

        // Gauge: Commandes par statut DELIVERED
        Gauge.builder("orders_by_status", orderRepository, repo ->
                repo.findByStatus(OrderStatus.DELIVERED).size())
                .tag("status", "DELIVERED")
                .description("Nombre de commandes par statut")
                .register(meterRegistry);

        // Gauge: Commandes par statut CANCELLED
        Gauge.builder("orders_by_status", orderRepository, repo ->
                repo.findByStatus(OrderStatus.CANCELLED).size())
                .tag("status", "CANCELLED")
                .description("Nombre de commandes par statut")
                .register(meterRegistry);

        // Gauge: Montant total des commandes du jour
        Gauge.builder("orders_today_total_amount", this, config -> {
            LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
            return orderRepository.findAll().stream()
                    .filter(order -> order.getOrderDate() != null 
                            && order.getOrderDate().isAfter(startOfDay) 
                            && order.getOrderDate().isBefore(endOfDay))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .doubleValue();
        })
                .description("Montant total des commandes du jour en euros")
                .register(meterRegistry);

        // Gauge: Nombre de commandes du jour
        Gauge.builder("orders_today_count", this, config -> {
            LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
            return orderRepository.findAll().stream()
                    .filter(order -> order.getOrderDate() != null 
                            && order.getOrderDate().isAfter(startOfDay) 
                            && order.getOrderDate().isBefore(endOfDay))
                    .count();
        })
                .description("Nombre de commandes du jour")
                .register(meterRegistry);

        // Compteur de commandes créées
        ordersCreatedCounter = Counter.builder("orders_created_total")
                .description("Nombre total de commandes créées")
                .register(meterRegistry);
    }

    /**
     * Incrémente le compteur de commandes créées
     */
    public void incrementOrderCreated() {
        ordersCreatedCounter.increment();
    }
}
