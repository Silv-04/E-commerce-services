package com.episen.ms_product.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.episen.ms_product.domain.enumerate.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'un produit.
 * Best practices :
 * - Séparation Request/Response
 * - Formatage JSON cohérent pour les dates
 * - Exposition sélective des champs (pas de données sensibles)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Category category;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
