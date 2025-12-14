package com.ecommerce.order.infrastructure.web.controller;

import com.ecommerce.order.application.dto.*;
import com.ecommerce.order.application.service.OrderService;
import com.ecommerce.order.domain.enumerate.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Commandes", description = "API de gestion des commandes")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les commandes", 
               description = "Retourne la liste de toutes les commandes avec leurs détails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des commandes récupérée avec succès")
    })
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("GET /api/orders - Récupération de toutes les commandes");
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une commande par son ID", 
               description = "Retourne les détails d'une commande spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commande trouvée"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        log.info("GET /api/orders/{} - Récupération de la commande", id);
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les commandes d'un utilisateur", 
               description = "Retourne la liste des commandes pour un utilisateur donné")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des commandes de l'utilisateur récupérée")
    })
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long userId) {
        log.info("GET /api/orders/user/{} - Récupération des commandes de l'utilisateur", userId);
        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les commandes par statut", 
               description = "Retourne la liste des commandes ayant un statut spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des commandes par statut récupérée"),
            @ApiResponse(responseCode = "400", description = "Statut invalide")
    })
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(
            @Parameter(description = "Statut de la commande (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)") 
            @PathVariable String status) {
        log.info("GET /api/orders/status/{} - Récupération des commandes par statut", status);
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle commande", 
               description = "Crée une nouvelle commande avec les items spécifiés. Vérifie la disponibilité du stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commande créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou produit non trouvé"),
            @ApiResponse(responseCode = "409", description = "Stock insuffisant")
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO requestDTO) {
        log.info("POST /api/orders - Création d'une nouvelle commande pour l'utilisateur {}", 
                requestDTO.getUserId());
        OrderResponseDTO createdOrder = orderService.createOrder(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une commande", 
               description = "Modifie le statut d'une commande existante (avec validation des transitions)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Transition de statut invalide"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @Parameter(description = "ID de la commande") @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO statusDTO) {
        log.info("PATCH /api/orders/{}/status - Mise à jour du statut: {}", id, statusDTO.getStatus());
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, statusDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une commande", 
               description = "Supprime une commande et restaure le stock si nécessaire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Commande supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Commande non trouvée")
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID de la commande") @PathVariable Long id) {
        log.info("DELETE /api/orders/{} - Suppression de la commande", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/daily-total")
    @Operation(summary = "Obtenir le total des ventes du jour", 
               description = "Calcule le montant total des commandes pour une date donnée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculé avec succès")
    })
    public ResponseEntity<BigDecimal> getDailyTotal(
            @Parameter(description = "Date au format YYYY-MM-DD (défaut: aujourd'hui)") 
            @RequestParam(required = false) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        log.info("GET /api/orders/stats/daily-total?date={} - Calcul du total journalier", targetDate);
        BigDecimal total = orderService.getDailyTotal(targetDate);
        return ResponseEntity.ok(total);
    }
}
