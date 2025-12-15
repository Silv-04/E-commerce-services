package com.episen.ms_product.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.episen.ms_product.application.dto.ProductRequestDTO;
import com.episen.ms_product.application.dto.ProductResponseDTO;
import com.episen.ms_product.application.mapper.ProductMapper;
import com.episen.ms_product.domain.entity.Product;
import com.episen.ms_product.domain.enumerate.Category;
import com.episen.ms_product.domain.repository.ProductRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour la gestion des produits.
 * Best practices :
 * - @Transactional pour la gestion des transactions
 * - Logging avec SLF4J
 * - Métriques personnalisées avec Micrometer
 * - Gestion d'erreurs explicite avec exceptions métier
 * - Séparation de la logique métier du contrôleur
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MeterRegistry meterRegistry;
    
    /** 
     * Renvoie la liste de tous les produits
     * @return List<ProductResponseDTO>
     */
    public List<ProductResponseDTO> getAllProducts() {
        log.debug("Récupération de tous les produits");
        
        List<Product> products = productRepository.findAll();
        
        log.info("Nombre de produits récupérés: {}", products.size());
        
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Renvoie un produit à partir de son ID
     * @param id
     * @return ProductResponseDTO
     */
    public ProductResponseDTO getProductById(Long id) {
        log.debug("Récupération du produit avec l'ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Produit non trouvé avec l'ID: {}", id);
                    return new RuntimeException("Produit non trouvé");
                });
        
        log.info("Produit récupéré avec succès: {}", product.getName());
        
        return productMapper.toDTO(product);
    }
    
    /**
     * Créé un nouveau produit à partir des informations fournies
     * @param productRequestDTO
     * @return ProductResponseDTO
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        log.debug("Création d'un nouveau produit: {}", productRequestDTO.getName());
        
        Product product = productMapper.toEntity(productRequestDTO);
        Product savedProduct = productRepository.save(product);
        
        log.info("Produit créé avec succès: {}", savedProduct.getName());

        Counter.builder("product.created")
                .description("Nombre de produits créés")
                .tag("type", productRequestDTO.getCategory().name())
                .register(meterRegistry)
                .increment();

        return productMapper.toDTO(savedProduct);
    }

    /**
     * Met à jour un produit existant à partir de son ID et des nouvelles informations fournies
     * @param id
     * @param productRequestDTO
     * @return ProductResponseDTO
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        log.debug("Mise à jour du produit avec l'ID: {}", id);
        
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Produit non trouvé avec l'ID: {}", id);
                    return new RuntimeException("Produit non trouvé");
                });
        
        existingProduct.setName(productRequestDTO.getName());
        existingProduct.setDescription(productRequestDTO.getDescription());
        existingProduct.setPrice(productRequestDTO.getPrice());
        existingProduct.setStock(productRequestDTO.getStock());
        existingProduct.setCategory(productRequestDTO.getCategory());
        existingProduct.setImageUrl(productRequestDTO.getImageUrl());
        
        log.info("Produit mis à jour avec succès: {}", existingProduct.getName());
        
        return productMapper.toDTO(existingProduct);
    }

    /**
     * Désactive un produit (soft delete).
     * 
     * <p>Au lieu de supprimer physiquement le produit, on le désactive
     * pour préserver l'intégrité des commandes qui y font référence.
     * Un produit désactivé ne peut plus être commandé mais reste
     * visible dans l'historique des commandes.</p>
     * 
     * @param id l'identifiant du produit à désactiver
     * @throws RuntimeException si le produit n'existe pas
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Désactivation du produit avec l'ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Produit non trouvé avec l'ID: {}", id);
                    return new RuntimeException("Produit non trouvé");
                });
        
        // Soft delete : on désactive le produit au lieu de le supprimer
        // pour préserver l'intégrité des commandes existantes
        product.setActive(false);
        productRepository.save(product);
        
        log.info("Produit désactivé avec succès avec l'ID: {}", id);
    }

    /**
     * Renvoie les informations des produits correspondant au nom donné
     * @param name
     * @return List<ProductResponseDTO>
     */
    public List<ProductResponseDTO> getProductByName(String name) {
        log.debug("Récupération du produit avec le nom: {}", name);
        
        List<Product> products = productRepository.findByName(name);
        
        if (products.isEmpty()) {
            log.error("Produit non trouvé avec le nom: {}", name);
            throw new RuntimeException("Produit non trouvé");
        }
        
        Product product = products.get(0);
        
        log.info("Produit récupéré avec succès: {}", product.getName());
        
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Renvoie les produits d'une catégorie donnée
     * @param category
     * @return List<ProductResponseDTO>
     */
    public List<ProductResponseDTO> getProductsByCategory(String category) {
        log.debug("Récupération des produits dans la catégorie: {}", category);
        
        List<Product> products = productRepository.findByCategory(Category.valueOf(category));
        
        log.info("Nombre de produits récupérés dans la catégorie {}: {}", category, products.size());
        
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Renvoie les produits disponibles (stock > 0)
     * @return List<ProductResponseDTO>
     */
    public List<ProductResponseDTO> getAvailableProducts() {
        log.debug("Récupération des produits disponibles");
        
        List<Product> products = productRepository.findAvailable();
        
        log.info("Nombre de produits disponibles récupérés: {}", products.size());
        
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour le stock d'un produit à partir de son ID et de la quantité à ajouter
     * @param id
     * @param quantity
     * @return ProductResponseDTO
     */
    @Transactional
    public ProductResponseDTO updateStock(Long id, int quantity) {
        log.debug("Mise à jour du stock pour le produit avec l'ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Produit non trouvé avec l'ID: {}", id);
                    return new RuntimeException("Produit non trouvé");
                });
        
        product.setStock(quantity + product.getStock());
        
        log.info("Stock mis à jour avec succès pour le produit: {}", product.getName());
        
        return productMapper.toDTO(product);
    }
}
