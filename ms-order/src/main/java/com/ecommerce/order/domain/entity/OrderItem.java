package com.ecommerce.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entité JPA représentant un article (ligne) dans une commande.
 * 
 * <p>Chaque OrderItem fait référence à un produit du catalogue et stocke
 * une copie du nom et du prix au moment de la commande pour garantir
 * l'intégrité des données même si le produit est modifié par la suite.</p>
 * 
 * <p>Le sous-total (subtotal) est calculé automatiquement lors de la
 * persistance : subtotal = quantity × unitPrice</p>
 * 
 * <p>Relation : ManyToOne avec Order (plusieurs articles appartiennent à une commande)</p>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 * @see Order
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
