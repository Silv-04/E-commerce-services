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
 * Client HTTP pour la communication avec le microservice User (ms-membership).
 * 
 * <p>Ce client utilise WebClient (Spring WebFlux) pour effectuer des appels REST
 * synchrones vers le service User sur le port 8081.</p>
 * 
 * <p><b>Fonctionnalités :</b></p>
 * <ul>
 *   <li>Récupération d'un utilisateur par son ID</li>
 *   <li>Vérification de la disponibilité du service (health check)</li>
 *   <li>Gestion des timeouts et des erreurs de connexion</li>
 * </ul>
 * 
 * <p><b>Configuration :</b></p>
 * <ul>
 *   <li>services.user.url - URL de base du service User</li>
 *   <li>services.user.timeout - Timeout en secondes (défaut: 5)</li>
 * </ul>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 * @see UserDTO
 */
@Component
public class UserClient implements IUserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final WebClient webClient;
    private final Duration timeout;

    public UserClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.user.url}") String userServiceUrl,
            @Value("${services.user.timeout:5}") int timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(userServiceUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    public Optional<UserDTO> getUserById(Long userId) {
        log.info("Appel au service User pour récupérer l'utilisateur {}", userId);
        
        try {
            UserDTO user = webClient.get()
                    .uri("/api/v1/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .timeout(timeout)
                    .block();
            
            return Optional.ofNullable(user);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Utilisateur {} non trouvé", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service User: {}", e.getMessage());
            throw new ServiceUnavailableException("Service User indisponible");
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
            log.warn("Service User indisponible: {}", e.getMessage());
            return false;
        }
    }
}
