package com.ecommerce.order.infrastructure.client;

import java.util.Optional;

public interface IProductClient {

    /**
     * Récupère un produit par son identifiant.
     *
     * @param productId Identifiant du produit
     * @param jwtToken  Token JWT à passer dans l'appel HTTP
     * @return Optional contenant le produit si trouvé
     */
    Optional<ProductDTO> getProductById(Long productId, String jwtToken);

    /**
     * Met à jour le stock d'un produit.
     *
     * @param productId Identifiant du produit
     * @param newStock  Nouvelle quantité de stock
     * @param jwtToken  Token JWT à passer dans l'appel HTTP
     * @return true si la mise à jour a réussi
     */
    boolean updateStock(Long productId, Integer newStock, String jwtToken);

    /**
     * Vérifie si le service Product est disponible.
     *
     * @return true si le service est disponible
     */
    boolean isServiceAvailable();
}
