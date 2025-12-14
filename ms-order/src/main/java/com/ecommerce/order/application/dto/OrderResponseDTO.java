package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.enumerate.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponseDTO() {}

    public OrderResponseDTO(Long id, Long userId, LocalDateTime orderDate, OrderStatus status,
                            BigDecimal totalAmount, String shippingAddress, List<OrderItemResponseDTO> items,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItemResponseDTO> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setItems(List<OrderItemResponseDTO> items) { this.items = items; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static OrderResponseDTOBuilder builder() { return new OrderResponseDTOBuilder(); }

    public static class OrderResponseDTOBuilder {
        private Long id;
        private Long userId;
        private LocalDateTime orderDate;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private List<OrderItemResponseDTO> items;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public OrderResponseDTOBuilder id(Long id) { this.id = id; return this; }
        public OrderResponseDTOBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderResponseDTOBuilder orderDate(LocalDateTime orderDate) { this.orderDate = orderDate; return this; }
        public OrderResponseDTOBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderResponseDTOBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderResponseDTOBuilder shippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; return this; }
        public OrderResponseDTOBuilder items(List<OrderItemResponseDTO> items) { this.items = items; return this; }
        public OrderResponseDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponseDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OrderResponseDTO build() {
            return new OrderResponseDTO(id, userId, orderDate, status, totalAmount, shippingAddress, items, createdAt, updatedAt);
        }
    }
}
