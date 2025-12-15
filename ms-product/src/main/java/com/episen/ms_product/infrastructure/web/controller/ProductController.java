package com.episen.ms_product.infrastructure.web.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.episen.ms_product.application.service.ProductService;
import com.episen.ms_product.application.dto.ProductRequestDTO;
import com.episen.ms_product.application.dto.ProductResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API de gestion des produits")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Récupérer tous les produits", description = "Retourne la liste complète de tous les produits enregistrés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("GET /api/v1/products - Récupération de tous les produits");

        List<ProductResponseDTO> products = productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Récupérer un produit par ID", description = "Retourne les détails d'un produit spécifique en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit récupéré avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé", content = @Content)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "ID du produit", required = true) @PathVariable Long id) {

        log.info("GET /api/v1/products/{} - Récupération du produit", id);

        ProductResponseDTO product = productService.getProductById(id);

        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Créer un nouveau produit", description = "Permet de créer un nouveau produit avec les données fournies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produit créé avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Parameter(description = "Données du produit à créer", required = true) @Valid @RequestBody ProductRequestDTO productRequestDTO) {

        log.warn("POST /api/v1/products - Création d'un produit: {}", productRequestDTO.getName());
        System.out.println("Creating product: " + productRequestDTO.getName());
        System.out.println("Product details: " + productRequestDTO.toString());

        ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdProduct);
    }

    @Operation(summary = "Mettre à jour un produit existant", description = "Permet de mettre à jour les informations d'un produit existant en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "ID du produit à mettre à jour", required = true) @PathVariable Long id,
            @Parameter(description = "Données du produit à mettre à jour", required = true) @Valid @RequestBody ProductRequestDTO productRequestDTO
    ) {
        log.info("PUT /api/v1/products/{} - Mise à jour du produit: {}", id, productRequestDTO.getName());

        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);

        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Supprimer un produit", description = "Permet de supprimer un produit en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé", content = @Content)
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> deleteProduct(
            @Parameter(description = "ID du produit à supprimer", required = true) @PathVariable Long id) {

        log.info("DELETE /api/v1/products/{} - Suppression du produit", id);

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer un produit par nom", description = "Retourne les détails d'un produit spécifique en fonction de son nom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit récupéré avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé", content = @Content)
    })
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getProductByName(
        @Parameter(description = "Nom du produit", required = true) @RequestParam("name") String name
    ) {
        log.info("GET /api/v1/products/ - Récupération du produit par nom", name);

        List<ProductResponseDTO> product = productService.getProductByName(name);

        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Récupérer des produits par catégorie", description = "Retourne la liste des produits appartenant à une catégorie spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produits récupérés avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Aucun produit trouvé dans cette catégorie", content = @Content)
    })
    @GetMapping(value = "/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getProductByCategory(
            @Parameter(description = "Catégorie du produit", required = true) @PathVariable String category
    ) {
        log.info("GET /api/v1/products/category/ - Récupération du produit par catégorie", category);

        List<ProductResponseDTO> product = productService.getProductsByCategory(category.toUpperCase());

        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Récupérer les produits disponibles", description = "Retourne la liste des produits avec un stock supérieur à zéro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produits disponibles récupérés avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getAvailableProducts() {
        log.info("GET /api/v1/products/available - Récupération des produits disponibles");

        List<ProductResponseDTO> products = productService.getAvailableProducts();

        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Mettre à jour le stock d'un produit", description = "Permet de mettre à jour le stock d'un produit en fonction de son ID et de la quantité à ajouter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock mis à jour avec succès", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé", content = @Content)
    })
    @PatchMapping(value = "/{id}/stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> updateStock(
        @Parameter(description = "ID du produit dont le stock doit être mis à jour", required = true) @PathVariable Long id,
        @Parameter(description = "Quantité à ajouter au stock", required = true) @RequestBody HashMap<String, Integer> quantity
    ) {
        log.info("PUT /api/v1/products/{}/stock - Mise à jour du stock du produit", id);

        ProductResponseDTO updatedProduct = productService.updateStock(id, quantity.get("quantity"));

        return ResponseEntity.ok(updatedProduct);
    }
}