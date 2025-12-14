package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.infrastructure.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Optional;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final WebClient webClient;
    private final Duration timeout;

    public ProductClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.product.url}") String productServiceUrl,
            @Value("${services.product.timeout:5}") int timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(productServiceUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    public Optional<ProductDTO> getProductById(Long productId) {
        log.info("Appel au service Product pour récupérer le produit {}", productId);
        
        try {
            ProductDTO product = webClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .timeout(timeout)
                    .block();
            
            return Optional.ofNullable(product);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Produit {} non trouvé", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service Product: {}", e.getMessage());
            throw new ServiceUnavailableException("Service Product indisponible");
        }
    }

    public boolean updateStock(Long productId, Integer newStock) {
        log.info("Mise à jour du stock du produit {} à {}", productId, newStock);
        
        try {
            webClient.patch()
                    .uri("/api/v1/products/{id}/stock", productId)
                    .bodyValue(java.util.Map.of("quantity", newStock))
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .timeout(timeout)
                    .block();
            
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du stock: {}", e.getMessage());
            return false;
        }
    }

    public boolean isServiceAvailable() {
        try {
            webClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Service Product indisponible: {}", e.getMessage());
            return false;
        }
    }
}
