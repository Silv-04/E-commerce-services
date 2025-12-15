package com.ecommerce.order.application.mapper;


import com.ecommerce.order.application.dto.OrderItemResponseDTO;
import com.ecommerce.order.application.dto.OrderResponseDTO;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour la conversion entre entités Order/OrderItem et leurs DTOs.
 * 
 * <p>Ce composant gère la transformation des objets du domaine vers les DTOs
 * utilisés dans les réponses API. Il assure une séparation claire entre
 * la couche domaine et la couche présentation.</p>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 * @see OrderResponseDTO
 * @see OrderItemResponseDTO
 */
@Component
public class OrderMapper {

    public OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) return null;

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(toItemResponseDTOList(order.getItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<OrderResponseDTO> toResponseDTOList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        if (item == null) return null;

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public List<OrderItemResponseDTO> toItemResponseDTOList(List<OrderItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList());
    }
}
