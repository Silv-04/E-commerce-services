package com.episen.ms_product.application.service;

import com.episen.ms_product.application.dto.ProductRequestDTO;
import com.episen.ms_product.application.dto.ProductResponseDTO;
import com.episen.ms_product.application.mapper.ProductMapper;
import com.episen.ms_product.domain.entity.Product;
import com.episen.ms_product.domain.enumerate.Category;
import com.episen.ms_product.domain.repository.ProductRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ProductService
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private MeterRegistry meterRegistry;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        productService = new ProductService(productRepository, productMapper, meterRegistry);
    }

    /*
     * getAllProducts
     */
    @Test
    @DisplayName("Doit retourner tous les produits")
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        Product product = createProduct();
        ProductResponseDTO dto = createProductResponseDTO();

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        // When
        List<ProductResponseDTO> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findAll();
    }

    /*
     * getProductById
     */
    @Test
    @DisplayName("Doit retourner un produit existant par ID")
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        Product product = createProduct();
        ProductResponseDTO dto = createProductResponseDTO();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        // When
        ProductResponseDTO result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Produit Test");
    }

    @Test
    @DisplayName("Doit lever une exception si le produit n'existe pas")
    void getProductById_WhenNotFound_ShouldThrowException() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produit non trouvé");
    }

    /*
     * createProduct
     */
    @Test
    @DisplayName("Doit créer un produit avec succès")
    void createProduct_WithValidData_ShouldCreateProduct() {
        // Given
        ProductRequestDTO request = createProductRequestDTO();
        Product product = createProduct();
        ProductResponseDTO dto = createProductResponseDTO();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(dto);

        // When
        ProductResponseDTO result = productService.createProduct(request);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).save(product);
    }

    /*
     * updateProduct
     */
    @Test
    @DisplayName("Doit mettre à jour un produit existant")
    void updateProduct_WhenProductExists_ShouldUpdateProduct() {
        // Given
        Product product = createProduct();
        ProductRequestDTO request = createProductRequestDTO();
        ProductResponseDTO dto = createProductResponseDTO();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        // When
        ProductResponseDTO result = productService.updateProduct(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(product.getName()).isEqualTo(request.getName());
    }

    @Test
    @DisplayName("Doit lever une exception si le produit à mettre à jour n'existe pas")
    void updateProduct_WhenNotFound_ShouldThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> productService.updateProduct(1L, createProductRequestDTO()))
                .isInstanceOf(RuntimeException.class);
    }

    /*
     * deleteProduct
     */
    @Test
    @DisplayName("Doit désactiver un produit (soft delete)")
    void deleteProduct_ShouldDisableProduct() {
        // Given
        Product product = createProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        productService.deleteProduct(1L);

        // Then
        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    /*
     * getProductByName
     */
    @Test
    @DisplayName("Doit retourner les produits par nom")
    void getProductByName_WhenExists_ShouldReturnProducts() {
        // Given
        Product product = createProduct();
        ProductResponseDTO dto = createProductResponseDTO();

        when(productRepository.findByName("Produit Test")).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        // When
        List<ProductResponseDTO> result = productService.getProductByName("Produit Test");

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Doit lever une exception si aucun produit ne correspond au nom")
    void getProductByName_WhenEmpty_ShouldThrowException() {
        // Given
        when(productRepository.findByName("Unknown")).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> productService.getProductByName("Unknown"))
                .isInstanceOf(RuntimeException.class);
    }

    /*
     * updateStock
     */
    @Test
    @DisplayName("Doit mettre à jour le stock d'un produit")
    void updateStock_ShouldIncreaseStock() {
        // Given
        Product product = createProduct();
        ProductResponseDTO dto = createProductResponseDTO();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        // When
        productService.updateStock(1L, 5);

        // Then
        assertThat(product.getStock()).isEqualTo(15);
    }

    /*
     * Helpers
     */
    private Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Produit Test");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setStock(10);
        product.setCategory(Category.FOOD);
        product.setActive(true);
        return product;
    }

    private ProductRequestDTO createProductRequestDTO() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Produit Test");
        dto.setPrice(BigDecimal.valueOf(29.99));
        dto.setStock(10);
        dto.setCategory(Category.FOOD);
        return dto;
    }

    private ProductResponseDTO createProductResponseDTO() {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setName("Produit Test");
        return dto;
    }
}
