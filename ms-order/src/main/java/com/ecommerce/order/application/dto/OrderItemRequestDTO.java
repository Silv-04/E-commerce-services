package com.ecommerce.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequestDTO {

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantity;

    public OrderItemRequestDTO() {}

    public OrderItemRequestDTO(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }

    public void setProductId(Long productId) { this.productId = productId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public static OrderItemRequestDTOBuilder builder() { return new OrderItemRequestDTOBuilder(); }

    public static class OrderItemRequestDTOBuilder {
        private Long productId;
        private Integer quantity;

        public OrderItemRequestDTOBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderItemRequestDTOBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }

        public OrderItemRequestDTO build() { return new OrderItemRequestDTO(productId, quantity); }
    }
}
