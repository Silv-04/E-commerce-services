-- Données initiales pour le service Order
-- Ce fichier initialise les données de test

-- Création de la table orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Création de la table order_items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Insertion des données de test - Commandes
INSERT INTO orders (id, user_id, order_date, status, total_amount, shipping_address, created_at, updated_at) VALUES
(1, 1, '2024-12-10 10:30:00', 'DELIVERED', 1299.99, '123 Rue de Paris, 75001 Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '2024-12-12 14:45:00', 'SHIPPED', 89.97, '123 Rue de Paris, 75001 Paris', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, '2024-12-13 09:15:00', 'CONFIRMED', 549.99, '45 Avenue des Champs, 69001 Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, '2024-12-14 16:20:00', 'PENDING', 199.98, '78 Boulevard Maritime, 13001 Marseille', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, '2024-12-15 11:00:00', 'PENDING', 79.99, '45 Avenue des Champs, 69001 Lyon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertion des données de test - Articles de commande
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
-- Commande 1 : iPhone
(1, 1, 1, 'iPhone 15 Pro', 1, 1299.99, 1299.99),
-- Commande 2 : T-shirts
(2, 2, 3, 'T-Shirt Premium Coton', 3, 29.99, 89.97),
-- Commande 3 : MacBook
(3, 3, 2, 'MacBook Air M2', 1, 549.99, 549.99),
-- Commande 4 : Jeans
(4, 4, 4, 'Jean Slim Fit', 2, 99.99, 199.98),
-- Commande 5 : Casque
(5, 5, 5, 'Casque Bluetooth Sony', 1, 79.99, 79.99);

-- Réinitialiser les séquences auto-increment pour éviter les conflits de clé primaire
ALTER TABLE orders ALTER COLUMN id RESTART WITH 100;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 100;
