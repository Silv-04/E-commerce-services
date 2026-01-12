#!/bin/bash

# Setup E-Commerce Platform - Initialisation complète
# Ce script configure et lance la plateforme e-commerce avec sécurité JWT/RSA

set -e

echo "=========================================="
echo "Setup E-Commerce Platform"
echo "=========================================="
echo ""

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les erreurs
error() {
    echo -e "${RED}✗ $1${NC}"
    exit 1
}

# Fonction pour afficher les succès
success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Fonction pour afficher les infos
info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# 1. Vérifier les prérequis
echo "[1/5] Vérification des prérequis..."
echo ""

if ! command -v docker &> /dev/null; then
    error "Docker n'est pas installé. Veuillez installer Docker Desktop."
fi
success "Docker est installé ($(docker --version))"

if ! command -v docker-compose &> /dev/null; then
    error "Docker Compose n'est pas installé."
fi
success "Docker Compose est installé ($(docker-compose --version))"

if ! command -v openssl &> /dev/null; then
    error "OpenSSL n'est pas installé."
fi
success "OpenSSL est installé"

echo ""

# 2. Créer les clés RSA
echo "[2/5] Génération des clés RSA..."
echo ""

KEYS_DIR="$PROJECT_DIR/keys"

if [ -f "$KEYS_DIR/server.p12" ]; then
    info "Les clés RSA existent déjà ($KEYS_DIR/server.p12)"
    read -p "Régénérer les clés ? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        success "Clés existantes conservées"
        echo ""
    else
        rm -f "$KEYS_DIR"/*.{key,crt,p12}
        info "Génération des nouvelles clés..."
    fi
else
    mkdir -p "$KEYS_DIR"
    info "Création du répertoire des clés..."
fi

if [ ! -f "$KEYS_DIR/server.p12" ]; then
    # Générer les clés
    info "Création de la clé privée RSA (2048 bits)..."
    openssl genrsa -out "$KEYS_DIR/private.key" 2048 > /dev/null 2>&1
    success "Clé privée créée"

    info "Création du certificat auto-signé (365 jours)..."
    openssl req -new -x509 -key "$KEYS_DIR/private.key" -out "$KEYS_DIR/certificate.crt" \
        -days 365 -subj "/C=FR/ST=IDF/L=Paris/O=Episen/CN=e-commerce" > /dev/null 2>&1
    success "Certificat créé"

    info "Conversion en format PKCS12..."
    openssl pkcs12 -export -in "$KEYS_DIR/certificate.crt" -inkey "$KEYS_DIR/private.key" \
        -out "$KEYS_DIR/server.p12" -name jil -password pass:jil > /dev/null 2>&1
    success "Clés RSA créées"

    # Nettoyer les fichiers intermédiaires
    rm -f "$KEYS_DIR/private.key" "$KEYS_DIR/certificate.crt"
fi

success "Clés RSA disponibles à $KEYS_DIR/server.p12"
echo ""

# 3. Builder les images Docker
echo "[3/5] Build des images Docker..."
echo ""

info "Compilation et build des services..."

# Membership
info "Building ms-membership..."
cd "$PROJECT_DIR/ms-membership"
mvn clean package -DskipTests -q || error "Erreur lors du build de ms-membership"
docker build -t ecommerce-membership:1.0 . > /dev/null 2>&1
success "ms-membership construit"

# Product
info "Building ms-product..."
cd "$PROJECT_DIR/ms-product"
mvn clean package -DskipTests -q || error "Erreur lors du build de ms-product"
docker build -t ecommerce-product:1.0 . > /dev/null 2>&1
success "ms-product construit"

# Order
info "Building ms-order..."
cd "$PROJECT_DIR/ms-order"
mvn clean package -DskipTests -q || error "Erreur lors du build de ms-order"
docker build -t ecommerce-order:1.0 . > /dev/null 2>&1
success "ms-order construit"

echo ""

# 4. Vérifier les images Docker
echo "[4/5] Vérification des images Docker..."
echo ""

docker images | grep ecommerce

echo ""
success "Images Docker créées"
echo ""

# 5. Démarrer les services
echo "[5/5] Démarrage des services..."
echo ""

cd "$PROJECT_DIR"
docker-compose down > /dev/null 2>&1 || true
info "Démarrage des services..."
docker-compose up -d

# Attendre que les services démarrent
info "Attente du démarrage des services (10s)..."
sleep 10

# Vérifier les services
echo ""
docker-compose ps

echo ""
echo "=========================================="
success "Setup complété avec succès!"
echo "=========================================="
echo ""
echo "Services accessibles:"
echo "  - Membership: http://localhost:8081"
echo "  - Product:    http://localhost:8082"
echo "  - Order:      http://localhost:8083"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana:    http://localhost:3000 (admin/admin123)"
echo ""
echo "Prochaines étapes:"
echo "  1. Importer la collection Postman: postman/platform-secured.json"
echo "  2. Créer un utilisateur via 3.1 Créer un utilisateur"
echo "  3. Faire un login via 3.2 Login - Récupérer le JWT"
echo "  4. Tester les endpoints sécurisés (4.1, 4.2)"
echo ""
echo "Documentation:"
echo "  - Architecture sécurité: SECURITY.md"
echo "  - Guide Docker: DOCKER.md"
echo "  - Résumé implémentation: IMPLEMENTATION.md"
echo ""
echo "Arrêter les services: docker-compose down"
echo "Voir les logs: docker-compose logs -f [service-name]"
echo ""
