package com.ecommerce.order.application.dto;

import java.math.BigDecimal;

public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public OrderItemResponseDTO() {}

    public OrderItemResponseDTO(Long id, Long productId, String productName, 
                                Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }

    public void setId(Long id) { this.id = id; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public static OrderItemResponseDTOBuilder builder() { return new OrderItemResponseDTOBuilder(); }

    public static class OrderItemResponseDTOBuilder {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public OrderItemResponseDTOBuilder id(Long id) { this.id = id; return this; }
        public OrderItemResponseDTOBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderItemResponseDTOBuilder productName(String productName) { this.productName = productName; return this; }
        public OrderItemResponseDTOBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemResponseDTOBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public OrderItemResponseDTOBuilder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }

        public OrderItemResponseDTO build() {
            return new OrderItemResponseDTO(id, productId, productName, quantity, unitPrice, subtotal);
        }
    }
}
