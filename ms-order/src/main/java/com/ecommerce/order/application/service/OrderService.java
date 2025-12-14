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
import com.ecommerce.order.infrastructure.exception.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des commandes.
 * 
 * <p>Ce service implémente toute la logique métier liée aux commandes :</p>
 * <ul>
 *   <li>Création de commandes avec validation de l'utilisateur et des produits</li>
 *   <li>Gestion du stock (déduction à la création, restauration à l'annulation)</li>
 *   <li>Transitions de statut avec règles métier</li>
 *   <li>Communication inter-services via WebClient (User et Product services)</li>
 * </ul>
 * 
 * <p><b>Règles métier importantes :</b></p>
 * <ul>
 *   <li>Une commande doit contenir au moins un article</li>
 *   <li>L'utilisateur doit exister dans le service User</li>
 *   <li>Tous les produits doivent être en stock suffisant</li>
 *   <li>Les commandes DELIVERED ou CANCELLED ne peuvent plus être modifiées</li>
 * </ul>
 * 
 * <p><b>Métriques Prometheus :</b></p>
 * <ul>
 *   <li>orders.created.total - Compteur des commandes créées</li>
 *   <li>orders.cancelled.total - Compteur des commandes annulées</li>
 * </ul>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 * @see OrderMapper
 * @see UserClient
 * @see ProductClient
 */
@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final Counter ordersCreatedCounter;
    private final Counter ordersCancelledCounter;

    public OrderService(OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       OrderMapper orderMapper,
                       UserClient userClient, 
                       ProductClient productClient,
                       MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.userClient = userClient;
        this.productClient = productClient;
        this.ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Nombre total de commandes créées")
                .register(meterRegistry);
        this.ordersCancelledCounter = Counter.builder("orders.cancelled.total")
                .description("Nombre total de commandes annulées")
                .register(meterRegistry);
    }

    public List<OrderResponseDTO> getAllOrders() {
        log.info("Récupération de toutes les commandes");
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long id) {
        log.info("Récupération de la commande avec l'id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));
        return orderMapper.toResponseDTO(order);
    }

    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        log.info("Récupération des commandes pour l'utilisateur: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Récupération des commandes avec le statut: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        log.info("Création d'une nouvelle commande pour l'utilisateur: {}", requestDTO.getUserId());

        // Vérifier que l'utilisateur existe
        UserDTO user = userClient.getUserById(requestDTO.getUserId())
                .orElseThrow(() -> new BusinessException("Utilisateur avec l'id " + requestDTO.getUserId() + " n'existe pas"));
        log.debug("Utilisateur vérifié: {} {}", user.getFirstName(), user.getLastName());

        // Créer la commande
        Order order = Order.builder()
                .userId(requestDTO.getUserId())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .shippingAddress(requestDTO.getShippingAddress())
                .build();

        // Ajouter les items et vérifier le stock
        for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
            // Vérifier que le produit existe et a du stock
            ProductDTO product = productClient.getProductById(itemDTO.getProductId())
                    .orElseThrow(() -> new BusinessException("Produit avec l'id " + itemDTO.getProductId() + " n'existe pas"));

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new InsufficientStockException(itemDTO.getProductId(), itemDTO.getQuantity(), product.getStock());
            }

            // Créer l'item de commande
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemDTO.getProductId())
                    .productName(product.getName())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(orderItem);

            // Mettre à jour le stock
            int newStock = product.getStock() - itemDTO.getQuantity();
            productClient.updateStock(itemDTO.getProductId(), newStock);
            log.debug("Stock mis à jour pour le produit {}: {} -> {}", 
                    itemDTO.getProductId(), product.getStock(), newStock);
        }

        // Calculer le montant total
        order.calculateTotalAmount();

        // Sauvegarder la commande
        Order savedOrder = orderRepository.save(order);
        log.info("Commande créée avec succès: id={}, total={}", savedOrder.getId(), savedOrder.getTotalAmount());

        // Incrémenter le compteur
        ordersCreatedCounter.increment();

        return orderMapper.toResponseDTO(savedOrder);
    }

    public OrderResponseDTO updateOrderStatus(Long id, StatusUpdateDTO statusDTO) {
        log.info("Mise à jour du statut de la commande {}: {}", id, statusDTO.getStatus());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));

        OrderStatus newStatus = statusDTO.getStatus();
        OrderStatus currentStatus = order.getStatus();

        // Valider la transition de statut
        validateStatusTransition(currentStatus, newStatus);

        // Si annulation, restaurer le stock
        if (newStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.CANCELLED) {
            restoreStock(order);
            ordersCancelledCounter.increment();
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        log.info("Statut de la commande {} mis à jour: {} -> {}", id, currentStatus, newStatus);

        return orderMapper.toResponseDTO(updatedOrder);
    }

    public void deleteOrder(Long id) {
        log.info("Suppression de la commande: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));

        // Si la commande n'est pas annulée, restaurer le stock
        if (order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        orderRepository.delete(order);
        log.info("Commande {} supprimée avec succès", id);
    }

    public BigDecimal getDailyTotal(LocalDate date) {
        log.info("Calcul du total des ventes pour le: {}", date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        BigDecimal total = orderRepository.getTotalAmountForDay(startOfDay, endOfDay);
        return total != null ? total : BigDecimal.ZERO;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Règles de transition de statut
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // États finaux
        };

        if (!valid) {
            throw new BusinessException(
                    String.format("Transition de statut invalide: %s -> %s", current, next));
        }
    }

    private void restoreStock(Order order) {
        log.info("Restauration du stock pour la commande: {}", order.getId());
        for (OrderItem item : order.getItems()) {
            try {
                Optional<ProductDTO> productOpt = productClient.getProductById(item.getProductId());
                if (productOpt.isPresent()) {
                    ProductDTO product = productOpt.get();
                    int newStock = product.getStock() + item.getQuantity();
                    productClient.updateStock(item.getProductId(), newStock);
                    log.debug("Stock restauré pour le produit {}: {} -> {}", 
                            item.getProductId(), product.getStock(), newStock);
                }
            } catch (Exception e) {
                log.error("Erreur lors de la restauration du stock pour le produit {}: {}", 
                        item.getProductId(), e.getMessage());
            }
        }
    }
}
