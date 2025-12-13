package com.episen.ms_product.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.episen.ms_product.domain.enumerate.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom ne peut pas être vide")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "La description ne peut pas être vide")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @NotNull(message = "Le prix ne peut pas être nul")
    @Min(value = 0, message = "Le prix doit être positif")
    @Digits(integer = 10, fraction = 2, message = "Le prix doit avoir au maximum 2 décimales")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @NotNull(message = "Le stock ne peut pas être nul")
    @Min(value = 0, message = "Le stock doit être positif")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @NotNull(message = "La catégorie ne peut pas être vide")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Column(name = "active")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
