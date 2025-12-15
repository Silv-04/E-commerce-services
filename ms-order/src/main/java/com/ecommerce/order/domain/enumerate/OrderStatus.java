package com.ecommerce.order.domain.enumerate;

/**
 * Énumération représentant les différents statuts possibles d'une commande.
 * 
 * <p>Le cycle de vie d'une commande suit les transitions suivantes :</p>
 * <pre>
 * PENDING → CONFIRMED → SHIPPED → DELIVERED
 *    ↓          ↓
 * CANCELLED   CANCELLED
 * </pre>
 * 
 * <p><b>Règles de transition :</b></p>
 * <ul>
 *   <li>PENDING peut passer à CONFIRMED ou CANCELLED</li>
 *   <li>CONFIRMED peut passer à SHIPPED ou CANCELLED</li>
 *   <li>SHIPPED peut uniquement passer à DELIVERED</li>
 *   <li>DELIVERED et CANCELLED sont des états finaux (aucune transition possible)</li>
 * </ul>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 */
public enum OrderStatus {
    /** Commande en attente de confirmation - État initial */
    PENDING,
    /** Commande confirmée par le système */
    CONFIRMED,
    /** Commande expédiée au client */
    SHIPPED,
    /** Commande livrée au client - État final */
    DELIVERED,
    /** Commande annulée - État final */
    CANCELLED
}
