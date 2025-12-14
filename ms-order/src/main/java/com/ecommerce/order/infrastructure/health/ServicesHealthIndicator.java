package com.ecommerce.order.infrastructure.health;

import com.ecommerce.order.infrastructure.client.ProductClient;
import com.ecommerce.order.infrastructure.client.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Indicateur de santé personnalisé pour vérifier la disponibilité des services dépendants.
 * 
 * <p>Ce composant est utilisé par Spring Boot Actuator pour exposer l'état de santé
 * des services User et Product via l'endpoint /actuator/health.</p>
 * 
 * <p><b>Services vérifiés :</b></p>
 * <ul>
 *   <li>User Service (ms-membership) sur le port 8081</li>
 *   <li>Product Service (ms-product) sur le port 8082</li>
 * </ul>
 * 
 * <p><b>États possibles :</b></p>
 * <ul>
 *   <li>UP - Tous les services sont disponibles</li>
 *   <li>DOWN - Au moins un service est indisponible</li>
 * </ul>
 * 
 * <p>Accessible via : GET /actuator/health</p>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 * @see UserClient
 * @see ProductClient
 */
@Component
public class ServicesHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ServicesHealthIndicator.class);

    private final UserClient userClient;
    private final ProductClient productClient;

    public ServicesHealthIndicator(UserClient userClient, ProductClient productClient) {
        this.userClient = userClient;
        this.productClient = productClient;
    }

    @Override
    public Health health() {
        boolean userServiceUp = checkUserService();
        boolean productServiceUp = checkProductService();

        Health.Builder builder = userServiceUp && productServiceUp 
                ? Health.up() 
                : Health.down();

        return builder
                .withDetail("userService", userServiceUp ? "UP" : "DOWN")
                .withDetail("productService", productServiceUp ? "UP" : "DOWN")
                .build();
    }

    private boolean checkUserService() {
        try {
            userClient.getUserById(1L);
            return true;
        } catch (Exception e) {
            log.warn("User service health check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkProductService() {
        try {
            productClient.getProductById(1L);
            return true;
        } catch (Exception e) {
            log.warn("Product service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
