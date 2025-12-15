package com.episen.ms_product.application.mapper;

import org.springframework.stereotype.Component;

import com.episen.ms_product.application.dto.ProductRequestDTO;
import com.episen.ms_product.application.dto.ProductResponseDTO;
import com.episen.ms_product.domain.entity.Product;

/**
 * Mapper pour convertir entre Product et ses DTOs.
 * Best practices :
 * - Séparation de la logique de mapping
 * - Conversion centralisée
 * - Facilite les tests unitaires
 */
@Component
public class ProductMapper {
    
    /**
     * Convertit un ProductRequestDTO en entité Product
     */
    public Product toEntity(ProductRequestDTO productRequestDTO) {
        return Product.builder()
                .name(productRequestDTO.getName())
                .description(productRequestDTO.getDescription())
                .price(productRequestDTO.getPrice())
                .stock(productRequestDTO.getStock())
                .category(productRequestDTO.getCategory())
                .imageUrl(productRequestDTO.getImageUrl())
                .build();
    }

    /**
     * Convertit une entité Product en ProductResponseDTO
     */
    public ProductResponseDTO toDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .active(product.isActive())
                .build();
    }

    /**
     * Met à jour une entité Product existante avec les données du DTO
     */
    public void updateEntityFromDTO(ProductRequestDTO productRequestDTO, Product product) {
        product.setName(productRequestDTO.getName());
        product.setStock(productRequestDTO.getStock());
    }
}
