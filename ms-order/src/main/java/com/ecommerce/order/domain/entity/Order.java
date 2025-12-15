package com.ecommerce.order.domain.entity;

import com.ecommerce.order.domain.enumerate.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant une commande dans le système e-commerce.
 * 
 * <p>Une commande est associée à un utilisateur et contient plusieurs articles (OrderItem).
 * Elle possède un statut qui évolue au cours de son cycle de vie :
 * PENDING → CONFIRMED → SHIPPED → DELIVERED ou CANCELLED.</p>
 * 
 * <p>Relation : OneToMany avec OrderItem (une commande contient plusieurs articles)</p>
 * 
 * @author E-commerce Team
 * @version 1.0
 * @since 2024-12
 * @see OrderItem
 * @see OrderStatus
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .peek(OrderItem::calculateSubtotal) // S'assurer que le subtotal est calculé
                .map(OrderItem::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
