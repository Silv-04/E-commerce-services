package com.ecommerce.order.infrastructure.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super(String.format("Stock insuffisant pour le produit %d: demandé %d, disponible %d", 
                productId, requested, available));
    }
}
