package com.membership.users.infrastructure.security;

import com.membership.users.domain.entity.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenGenerator {

    private final RSAPrivateKey privateKey;

    public JwtTokenGenerator() {
        KeyPair keyPair = InfraSetting.keyPairLoader();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    /**
     * Générer un JWT token pour un utilisateur
     * @param user utilisateur authentifié
     * @return JWT token signé avec la clé privée RSA
     */
    public String generateToken(User user) {
        try {
            // Créer les claims
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("episen-e-commerce")
                .audience("web")
                .claim("UserId", user.getId())
                .claim("Email", user.getEmail())
                .claim("Roles", user.getRoles() != null ? user.getRoles() : java.util.Collections.emptyList())
                .expirationTime(new Date(Instant.now().plusSeconds(3600).toEpochMilli())) // 1 heure
                .issueTime(new Date())
                .build();

            // Créer le header
            JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

            // Créer le JWT signé
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // Signer avec la clé privée
            com.nimbusds.jose.crypto.RSASSASigner signer = 
                new com.nimbusds.jose.crypto.RSASSASigner(privateKey);
            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Erreur lors de la génération du JWT: " + e.getMessage(), e);
        }
    }

    /**
     * Valider un JWT token (vérifier la signature)
     * @param token JWT token
     * @throws RuntimeException si token invalide
     */
    public void validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // Vérifier la signature avec la clé publique
            com.nimbusds.jose.crypto.RSASSAVerifier verifier = 
                new com.nimbusds.jose.crypto.RSASSAVerifier((java.security.interfaces.RSAPublicKey) 
                    InfraSetting.keyPairLoader().getPublic());
            
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Signature du token invalide");
            }

            // Vérifier l'expiration
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                throw new RuntimeException("Token expiré");
            }

        } catch (Exception e) {
            throw new RuntimeException("Token invalide: " + e.getMessage(), e);
        }
    }
}
