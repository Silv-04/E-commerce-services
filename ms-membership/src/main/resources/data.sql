-- Données initiales pour la base de données H2
-- Ce script est exécuté automatiquement au démarrage de l'application
-- Création de la table users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL DEFAULT 'ROLE_USER',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO
    users (first_name, last_name, email, password_hash, roles)
VALUES (
        'Alice',
        'Martin',
        'alice.martin@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'Bob',
        'Dupont',
        'bob.dupont@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER,ROLE_ADMIN'
    ),
    (
        'Caroline',
        'Leclerc',
        'caroline.leclerc@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'David',
        'Moreau',
        'david.moreau@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'Elodie',
        'Girard',
        'elodie.girard@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'François',
        'Petit',
        'francois.petit@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'Gabriel',
        'Rousseau',
        'gabriel.rousseau@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'Hélène',
        'Faure',
        'helene.faure@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'Isabelle',
        'Blanc',
        'isabelle.blanc@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    ),
    (
        'Julien',
        'Lemoine',
        'julien.lemoine@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWo.O5pby1rH89Y8pZx8vD3K0N8BgQ4cQVO',
        'ROLE_USER'
    );