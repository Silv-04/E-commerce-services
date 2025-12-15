package com.ecommerce.order.infrastructure.client;

import java.util.Optional;

public interface IProductClient {
    Optional<ProductDTO> getProductById(Long productId);
    boolean updateStock(Long productId, Integer newStock);
    boolean isServiceAvailable();
}
