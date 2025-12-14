package com.episen.ms_product.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.episen.ms_product.domain.entity.Product;
import com.episen.ms_product.domain.enumerate.Category;

/**
 * Repository JPA pour l'entité Product.
 * 
 * <p>Fournit les opérations CRUD et des requêtes personnalisées
 * pour la gestion des produits du catalogue.</p>
 * 
 * @author E-commerce Team
 * @version 1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Recherche les produits par nom (recherche exacte).
     * @param name le nom du produit
     * @return liste des produits correspondants
     */
    List<Product> findByName(String name);

    /**
     * Recherche les produits par catégorie (uniquement les produits actifs).
     * @param category la catégorie recherchée
     * @return liste des produits actifs de cette catégorie
     */
    List<Product> findByCategoryAndActiveTrue(Category category);

    /**
     * Recherche les produits par catégorie (tous les produits).
     * @param category la catégorie recherchée
     * @return liste des produits de cette catégorie
     */
    List<Product> findByCategory(Category category);

    /**
     * Retourne tous les produits actifs avec du stock disponible.
     * @return liste des produits disponibles à la vente
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.active = true")
    List<Product> findAvailable();

    /**
     * Compte le nombre total de produits.
     * @return nombre de produits
     */
    @Query("SELECT COUNT(p) FROM Product p")
    long count();

    /**
     * Compte les produits dont le stock est inférieur à un seuil.
     * Utilisé pour le health check des produits en rupture.
     * @param amount le seuil de stock
     * @return nombre de produits sous ce seuil
     */
    long countByStockLessThanAndActiveTrue(int amount);
    
    long countByStockLessThan(int amount);

    /**
     * Retourne tous les produits actifs.
     * @return liste des produits actifs
     */
    List<Product> findByActiveTrue();
}
