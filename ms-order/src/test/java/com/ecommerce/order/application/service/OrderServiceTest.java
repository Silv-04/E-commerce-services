package com.ecommerce.order.application.service;

import com.ecommerce.order.application.dto.*;
import com.ecommerce.order.application.mapper.OrderMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.order.domain.enumerate.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.repository.OrderItemRepository;
import com.ecommerce.order.infrastructure.client.ProductClient;
import com.ecommerce.order.infrastructure.client.ProductDTO;
import com.ecommerce.order.infrastructure.client.UserClient;
import com.ecommerce.order.infrastructure.client.UserDTO;
import com.ecommerce.order.infrastructure.exception.BusinessException;
import com.ecommerce.order.infrastructure.exception.InsufficientStockException;
import com.ecommerce.order.infrastructure.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ProductClient productClient;

    private OrderMapper orderMapper;
    private MeterRegistry meterRegistry;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
        meterRegistry = new SimpleMeterRegistry();
        orderService = new OrderService(orderRepository, orderItemRepository, orderMapper, userClient, productClient, meterRegistry);
    }

    @Test
    @DisplayName("Doit retourner toutes les commandes")
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        Order order1 = createTestOrder(1L, OrderStatus.PENDING);
        Order order2 = createTestOrder(2L, OrderStatus.CONFIRMED);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // When
        List<OrderResponseDTO> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(2);
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Doit retourner une commande par ID")
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Given
        Order order = createTestOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        OrderResponseDTO result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Doit lever une exception si la commande n'existe pas")
    void getOrderById_WhenOrderNotExists_ShouldThrowException() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Doit créer une commande avec succès")
    void createOrder_WithValidData_ShouldCreateOrder() {
        // Given
        OrderRequestDTO request = createTestOrderRequest();
        UserDTO user = UserDTO.builder().id(1L).email("test@test.com").build();
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .name("Produit Test")
                .price(new BigDecimal("29.99"))
                .stock(10)
                .build();

        when(userClient.getUserById(1L)).thenReturn(Optional.of(user));
        when(productClient.getProductById(1L)).thenReturn(Optional.of(product));
        when(productClient.updateStock(anyLong(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // When
        OrderResponseDTO result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(productClient).updateStock(1L, 2);
    }

    @Test
    @DisplayName("Doit lever une exception si l'utilisateur n'existe pas")
    void createOrder_WhenUserNotExists_ShouldThrowException() {
        // Given
        OrderRequestDTO request = createTestOrderRequest();
        when(userClient.getUserById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Utilisateur");
    }

    @Test
    @DisplayName("Doit lever une exception si le stock est insuffisant")
    void createOrder_WhenInsufficientStock_ShouldThrowException() {
        // Given
        OrderRequestDTO request = createTestOrderRequest();
        UserDTO user = UserDTO.builder().id(1L).build();
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .name("Produit Test")
                .price(new BigDecimal("29.99"))
                .stock(1) // Stock insuffisant
                .build();

        when(userClient.getUserById(1L)).thenReturn(Optional.of(user));
        when(productClient.getProductById(1L)).thenReturn(Optional.of(product));

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Doit mettre à jour le statut d'une commande")
    void updateOrderStatus_WithValidStatus_ShouldUpdateOrder() {
        // Given
        Order order = createTestOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        StatusUpdateDTO statusUpdate = new StatusUpdateDTO(OrderStatus.CONFIRMED);

        // When
        OrderResponseDTO result = orderService.updateOrderStatus(1L, statusUpdate);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Doit lever une exception si la commande est déjà livrée")
    void updateOrderStatus_WhenDelivered_ShouldThrowException() {
        // Given
        Order order = createTestOrder(1L, OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        StatusUpdateDTO statusUpdate = new StatusUpdateDTO(OrderStatus.CANCELLED);

        // When/Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, statusUpdate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transition de statut invalide");
    }

    @Test
    @DisplayName("Doit annuler une commande avec succès")
    void cancelOrder_WithValidOrder_ShouldCancelOrder() {
        // Given
        Order order = createTestOrder(1L, OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        StatusUpdateDTO statusUpdate = new StatusUpdateDTO(OrderStatus.CANCELLED);

        // When
        orderService.updateOrderStatus(1L, statusUpdate);

        // Then
        verify(orderRepository).save(any(Order.class));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    // Helper methods
    private Order createTestOrder(Long id, OrderStatus status) {
        Order order = Order.builder()
                .id(id)
                .userId(1L)
                .orderDate(LocalDateTime.now())
                .status(status)
                .totalAmount(new BigDecimal("59.98"))
                .shippingAddress("123 Test Street")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Produit Test")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("59.98"))
                .build();

        order.addItem(item);
        return order;
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