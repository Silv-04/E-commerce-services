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
public class UserClient {

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
