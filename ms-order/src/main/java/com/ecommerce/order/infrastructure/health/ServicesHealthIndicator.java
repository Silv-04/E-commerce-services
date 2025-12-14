package com.ecommerce.order.infrastructure.health;

import com.ecommerce.order.infrastructure.client.ProductClient;
import com.ecommerce.order.infrastructure.client.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

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
