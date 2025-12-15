package com.episen.ms_product.infrastructure.metrics;

import com.episen.ms_product.domain.repository.ProductRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configuration des métriques personnalisées pour le service Product
 * Expose des métriques métier pour Prometheus/Grafana
 */
@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final ProductRepository productRepository;

    // Compteurs par catégorie
    private Counter electronicsCounter;
    private Counter clothingCounter;
    private Counter booksCounter;
    private Counter foodCounter;
    private Counter otherCounter;

    public MetricsConfig(MeterRegistry meterRegistry, ProductRepository productRepository) {
        this.meterRegistry = meterRegistry;
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void initMetrics() {
        // Gauge: Nombre total de produits
        Gauge.builder("products_total", productRepository, ProductRepository::count)
                .description("Nombre total de produits")
                .register(meterRegistry);

        // Gauge: Produits avec stock bas (< 5)
        Gauge.builder("products_low_stock", productRepository, repo -> 
                repo.findAll().stream()
                    .filter(p -> p.getStock() != null && p.getStock() < 5)
                    .count())
                .description("Nombre de produits avec stock bas (< 5)")
                .register(meterRegistry);

        // Gauge: Produits par catégorie ELECTRONICS
        Gauge.builder("products_by_category", productRepository, repo ->
                repo.findAll().stream()
                    .filter(p -> "ELECTRONICS".equals(p.getCategory()))
                    .count())
                .tag("category", "ELECTRONICS")
                .description("Nombre de produits par catégorie")
                .register(meterRegistry);

        // Gauge: Produits par catégorie CLOTHING
        Gauge.builder("products_by_category", productRepository, repo ->
                repo.findAll().stream()
                    .filter(p -> "CLOTHING".equals(p.getCategory()))
                    .count())
                .tag("category", "CLOTHING")
                .description("Nombre de produits par catégorie")
                .register(meterRegistry);

        // Gauge: Produits par catégorie BOOKS
        Gauge.builder("products_by_category", productRepository, repo ->
                repo.findAll().stream()
                    .filter(p -> "BOOKS".equals(p.getCategory()))
                    .count())
                .tag("category", "BOOKS")
                .description("Nombre de produits par catégorie")
                .register(meterRegistry);

        // Gauge: Produits par catégorie FOOD
        Gauge.builder("products_by_category", productRepository, repo ->
                repo.findAll().stream()
                    .filter(p -> "FOOD".equals(p.getCategory()))
                    .count())
                .tag("category", "FOOD")
                .description("Nombre de produits par catégorie")
                .register(meterRegistry);

        // Gauge: Produits par catégorie OTHER
        Gauge.builder("products_by_category", productRepository, repo ->
                repo.findAll().stream()
                    .filter(p -> "OTHER".equals(p.getCategory()))
                    .count())
                .tag("category", "OTHER")
                .description("Nombre de produits par catégorie")
                .register(meterRegistry);

        // Compteur de produits créés par catégorie
        electronicsCounter = Counter.builder("products_created_total")
                .tag("category", "ELECTRONICS")
                .description("Nombre de produits créés")
                .register(meterRegistry);

        clothingCounter = Counter.builder("products_created_total")
                .tag("category", "CLOTHING")
                .description("Nombre de produits créés")
                .register(meterRegistry);

        booksCounter = Counter.builder("products_created_total")
                .tag("category", "BOOKS")
                .description("Nombre de produits créés")
                .register(meterRegistry);

        foodCounter = Counter.builder("products_created_total")
                .tag("category", "FOOD")
                .description("Nombre de produits créés")
                .register(meterRegistry);

        otherCounter = Counter.builder("products_created_total")
                .tag("category", "OTHER")
                .description("Nombre de produits créés")
                .register(meterRegistry);
    }

    /**
     * Incrémente le compteur de produits créés pour une catégorie donnée
     */
    public void incrementProductCreated(String category) {
        switch (category) {
            case "ELECTRONICS" -> electronicsCounter.increment();
            case "CLOTHING" -> clothingCounter.increment();
            case "BOOKS" -> booksCounter.increment();
            case "FOOD" -> foodCounter.increment();
            default -> otherCounter.increment();
        }
    }
}
