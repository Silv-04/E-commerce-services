package com.ecommerce.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderRequestDTO {

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    private String shippingAddress;

    @NotEmpty(message = "La commande doit contenir au moins un article")
    @Valid
    private List<OrderItemRequestDTO> items;

    public OrderRequestDTO() {}

    public OrderRequestDTO(Long userId, String shippingAddress, List<OrderItemRequestDTO> items) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.items = items;
    }

    public Long getUserId() { return userId; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItemRequestDTO> getItems() { return items; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setItems(List<OrderItemRequestDTO> items) { this.items = items; }

    public static OrderRequestDTOBuilder builder() { return new OrderRequestDTOBuilder(); }

    public static class OrderRequestDTOBuilder {
        private Long userId;
        private String shippingAddress;
        private List<OrderItemRequestDTO> items;

        public OrderRequestDTOBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderRequestDTOBuilder shippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public OrderRequestDTOBuilder items(List<OrderItemRequestDTO> items) { this.items = items; return this; }

        public OrderRequestDTO build() { return new OrderRequestDTO(userId, shippingAddress, items); }
    }
}
