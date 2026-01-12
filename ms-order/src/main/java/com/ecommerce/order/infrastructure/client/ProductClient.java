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

/**
 * Client HTTP pour la communication avec le microservice Product (ms-product).
 *
 * <p>Ce client utilise WebClient (Spring WebFlux) pour effectuer des appels REST
 * synchrones vers le service Product sur le port 8082.</p>
 *
 * <p>Ce client accepte maintenant un token JWT à passer dans l'en-tête Authorization
 * pour tous les appels authentifiés.</p>
 *
 * <p><b>Fonctionnalités :</b></p>
 * <ul>
 *   <li>Récupération d'un produit par son ID</li>
 *   <li>Mise à jour du stock d'un produit (PATCH)</li>
 *   <li>Vérification de la disponibilité du service (health check)</li>
 *   <li>Gestion des timeouts et des erreurs de connexion</li>
 * </ul>
 *
 * <p><b>Configuration :</b></p>
 * <ul>
 *   <li>services.product.url - URL de base du service Product</li>
 *   <li>services.product.timeout - Timeout en secondes (défaut: 5)</li>
 * </ul>
 *
 * @author E-commerce Team
 * @version 1.1
 * @since 2024-12
 * @see ProductDTO
 */
@Component
public class ProductClient implements IProductClient {

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

    /**
     * Récupère un produit par son identifiant.
     *
     * @param productId Identifiant du produit
     * @param jwtToken  Token JWT à passer dans l'appel HTTP
     * @return Optional contenant le produit si trouvé
     */
    @Override
    public Optional<ProductDTO> getProductById(Long productId, String jwtToken) {
        log.info("Appel au service Product pour récupérer le produit {}", productId);

        try {
            ProductDTO product = webClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .header("Authorization", "Bearer " + jwtToken)
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

    /**
     * Met à jour le stock d'un produit.
     *
     * @param productId Identifiant du produit
     * @param newStock  Nouvelle quantité de stock
     * @param jwtToken  Token JWT à passer dans l'appel HTTP
     * @return true si la mise à jour a réussi
     */
    @Override
    public boolean updateStock(Long productId, Integer newStock, String jwtToken) {
        log.info("Mise à jour du stock du produit {} à {}", productId, newStock);

        try {
            webClient.patch()
                    .uri("/api/v1/products/{id}/stock", productId)
                    .header("Authorization", "Bearer " + jwtToken)
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

    /**
     * Vérifie si le service Product est disponible.
     *
     * @return true si le service est disponible
     */
    @Override
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
