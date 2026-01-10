-- Données initiales pour la base de données H2
-- Ce script est exécuté automatiquement au démarrage de l'application
-- Création de la table users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO
    users (first_name, last_name, email)
VALUES (
        'Alice',
        'Martin',
        'alice.martin@example.com'
    ),
    (
        'Bob',
        'Dupont',
        'bob.dupont@example.com'
    ),
    (
        'Caroline',
        'Leclerc',
        'caroline.leclerc@example.com'
    ),
    (
        'David',
        'Moreau',
        'david.moreau@example.com'
    ),
    (
        'Elodie',
        'Girard',
        'elodie.girard@example.com'
    ),
    (
        'François',
        'Petit',
        'francois.petit@example.com'
    ),
    (
        'Gabriel',
        'Rousseau',
        'gabriel.rousseau@example.com'
    ),
    (
        'Hélène',
        'Faure',
        'helene.faure@example.com'
    ),
    (
        'Isabelle',
        'Blanc',
        'isabelle.blanc@example.com'
    ),
    (
        'Julien',
        'Lemoine',
        'julien.lemoine@example.com'
    );