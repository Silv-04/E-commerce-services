package com.ecommerce.order.infrastructure.web.controller;

import com.ecommerce.order.application.dto.*;
import com.ecommerce.order.application.service.OrderService;
import com.ecommerce.order.domain.enumerate.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour OrderController.
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("GET /api/v1/orders doit retourner toutes les commandes")
    void getAllOrders_ShouldReturnOrders() throws Exception {
        // Given
        List<OrderResponseDTO> orders = Arrays.asList(
                createTestOrderResponse(1L, OrderStatus.PENDING),
                createTestOrderResponse(2L, OrderStatus.CONFIRMED)
        );
        when(orderService.getAllOrders()).thenReturn(orders);

        // When/Then
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} doit retourner une commande")
    void getOrderById_ShouldReturnOrder() throws Exception {
        // Given
        OrderResponseDTO order = createTestOrderResponse(1L, OrderStatus.PENDING);
        when(orderService.getOrderById(1L)).thenReturn(order);

        // When/Then
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/orders doit créer une commande")
    void createOrder_ShouldCreateOrder() throws Exception {
        // Given
        OrderRequestDTO request = createTestOrderRequest();
        OrderResponseDTO response = createTestOrderResponse(1L, OrderStatus.PENDING);
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/orders avec données invalides doit retourner 400")
    void createOrder_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - Request sans userId
        String invalidRequest = """
                {
                    "shippingAddress": "123 Test Street",
                    "items": [{"productId": 1, "quantity": 2}]
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/orders/{id}/status doit mettre à jour le statut")
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        // Given
        StatusUpdateDTO statusUpdate = new StatusUpdateDTO(OrderStatus.CONFIRMED);
        OrderResponseDTO response = createTestOrderResponse(1L, OrderStatus.CONFIRMED);
        when(orderService.updateOrderStatus(eq(1L), any(StatusUpdateDTO.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(put("/api/v1/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} doit annuler la commande")
    void cancelOrder_ShouldCancel() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/orders/user/{userId} doit retourner les commandes d'un utilisateur")
    void getOrdersByUserId_ShouldReturnUserOrders() throws Exception {
        // Given
        List<OrderResponseDTO> orders = Arrays.asList(createTestOrderResponse(1L, OrderStatus.PENDING));
        when(orderService.getOrdersByUserId(1L)).thenReturn(orders);

        // When/Then
        mockMvc.perform(get("/api/v1/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/orders/status/{status} doit filtrer par statut")
    void getOrdersByStatus_ShouldReturnFilteredOrders() throws Exception {
        // Given
        List<OrderResponseDTO> orders = Arrays.asList(
                createTestOrderResponse(1L, OrderStatus.PENDING),
                createTestOrderResponse(2L, OrderStatus.PENDING)
        );
        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(orders);

        // When/Then
        mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // Helper methods
    private OrderResponseDTO createTestOrderResponse(Long id, OrderStatus status) {
        OrderItemResponseDTO item = OrderItemResponseDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Produit Test")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("59.98"))
                .build();

        return OrderResponseDTO.builder()
                .id(id)
                .userId(1L)
                .orderDate(LocalDateTime.now())
                .status(status)
                .totalAmount(new BigDecimal("59.98"))
                .shippingAddress("123 Test Street")
                .items(Arrays.asList(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private OrderRequestDTO createTestOrderRequest() {
        OrderItemRequestDTO itemRequest = OrderItemRequestDTO.builder()
                .productId(1L)
                .quantity(2)
                .build();

        return OrderRequestDTO.builder()
                .userId(1L)
                .shippingAddress("123 Test Street")
                .items(Arrays.asList(itemRequest))
                .build();
    }
}
