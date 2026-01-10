-- Données initiales pour la base de données H2
-- Ce script est exécuté automatiquement au démarrage de l'application
-- Création de la table product
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    category VARCHAR(20) NOT NULL,
    imageUrl VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO
    product (
        name,
        description,
        price,
        stock,
        category,
        imageUrl
    )
VALUES (
        'Gaming Mouse',
        'Souris gamer ergonomique avec éclairage RGB',
        59.99,
        35,
        'ELECTRONICS',
        'https://example.com/gaming_mouse.jpg'
    ),
    (
        'Wireless Keyboard',
        'Clavier sans fil mécanique',
        89.90,
        25,
        'ELECTRONICS',
        'https://example.com/keyboard.jpg'
    ),
    (
        'E-book Reader',
        'Liseuse électronique avec écran e-ink',
        129.50,
        18,
        'ELECTRONICS',
        'https://example.com/ebook_reader.jpg'
    ),
    (
        'Organic Tea',
        'Thé bio en sachets individuels',
        8.99,
        120,
        'FOOD',
        'https://example.com/organic_tea.jpg'
    ),
    (
        'Pasta Pack',
        'Paquet de pâtes italiennes 500g',
        3.50,
        75,
        'FOOD',
        'https://example.com/pasta.jpg'
    ),
    (
        'Energy Bar',
        'Barre énergétique aux fruits secs',
        2.80,
        90,
        'FOOD',
        'https://example.com/energy_bar.jpg'
    ),
    (
        'Desk Lamp',
        'Lampe de bureau LED réglable',
        35.00,
        40,
        'OTHER',
        'https://example.com/desk_lamp.jpg'
    ),
    (
        'Planner 2025',
        'Agenda 2025 pour organiser vos journées',
        14.90,
        60,
        'OTHER',
        'https://example.com/planner.jpg'
    ),
    (
        'Wireless Charger',
        'Chargeur sans fil rapide pour smartphone',
        29.99,
        30,
        'ELECTRONICS',
        'https://example.com/wireless_charger.jpg'
    ),
    (
        'Bluetooth Speaker',
        'Enceinte Bluetooth portable et étanche',
        79.90,
        22,
        'ELECTRONICS',
        'https://example.com/speaker.jpg'
    );