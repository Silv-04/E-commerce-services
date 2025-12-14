package com.ecommerce.order.domain.enumerate;

/**
 * Enum représentant les différents statuts possibles d'une commande.
 */
public enum OrderStatus {
    PENDING,      // Commande en attente de confirmation
    CONFIRMED,    // Commande confirmée
    SHIPPED,      // Commande expédiée
    DELIVERED,    // Commande livrée
    CANCELLED     // Commande annulée
}
