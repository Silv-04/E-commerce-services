package com.membership.users.infrastructure.metrics;

import com.membership.users.domain.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configuration des métriques personnalisées pour le service Membership
 * Expose des métriques métier pour Prometheus/Grafana
 */

@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final UserRepository userRepository;

    // Compteur d'utilisateurs créés
    private Counter usersCreatedCounter;

    public MetricsConfig(MeterRegistry meterRegistry, UserRepository userRepository) {
        this.meterRegistry = meterRegistry;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initMetrics() {
        // Gauge: Nombre total d'utilisateurs
        Gauge.builder("users_total", userRepository, UserRepository::count)
                .description("Nombre total d'utilisateurs")
                .register(meterRegistry);

        // Gauge: Nombre d'utilisateurs actifs
        Gauge.builder("users_active", userRepository, repo ->
                repo.findAll().stream()
                    .filter(user -> user.getActive() != null && user.getActive())
                    .count())
                .description("Nombre d'utilisateurs actifs")
                .register(meterRegistry);

        // Compteur d'utilisateurs créés
        usersCreatedCounter = Counter.builder("users_created_total")
                .description("Nombre total d'utilisateurs créés")
                .register(meterRegistry);
    }

    /**
     * Incrémente le compteur d'utilisateurs créés
     */
    public void incrementUserCreated() {
        usersCreatedCounter.increment();
    }
}
