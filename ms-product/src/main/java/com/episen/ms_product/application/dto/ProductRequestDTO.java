package com.episen.ms_product.application.dto;

import java.math.BigDecimal;

import com.episen.ms_product.domain.enumerate.Category;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {
    @NotBlank(message = "Le nom ne peut pas être vide")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String name;

    @NotBlank(message = "La description ne peut pas être vide")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    private String description;

    @NotNull(message = "Le prix ne peut pas être nul")
    @DecimalMin(value = "0.01", inclusive = true, message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2, message = "Le prix doit avoir au maximum 2 décimales")
    private BigDecimal price;

    @NotNull(message = "Le stock ne peut pas être nul")
    @Min(value = 0, message = "Le stock doit être positif")
    private Integer stock;

    @NotNull(message = "La catégorie ne peut pas être vide")
    private Category category;

    private String imageUrl;
}
