package com.episen.ms_product.security;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.episen.ms_product.domain.entity.User;
import com.episen.ms_product.settings.InfraSetting;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.PostConstruct;

/**
 * Service de validation et de transformation des tokens JWT.
 */
@Service
public class JwtTokenValidator {
    private JWSVerifier jwsVerifier;

    /**
     * Initialise le vérificateur JWS avec la clé publique.
     */
    @PostConstruct
    public void init() {
        jwsVerifier = new RSASSAVerifier((RSAPublicKey) InfraSetting.keyPairLoader().getPublic());
    }

    /**
     * Valide l'audience du token JWT.
     * @param claimsSet Les revendications du token JWT.
     * @return true si l'audience est valide, false sinon.
     */
    private boolean validateTokenAudience(JWTClaimsSet claimsSet) {
        return claimsSet.getAudience().contains("web");
    }

    /**
     * Valide l'expiration du token JWT.
     * @param claimsSet Les revendications du token JWT.
     * @return true si le token n'est pas expiré, false sinon.
     */
    private boolean validateTokenExpiration(JWTClaimsSet claimsSet) {
        return Instant.now().isBefore(claimsSet.getExpirationTime().toInstant());
    }

    /**
     * Valide l'émetteur du token JWT.
     * @param claimsSet Les revendications du token JWT.
     * @return true si l'émetteur est valide, false sinon.
     */
    private boolean validateTokenIssuer(JWTClaimsSet claimsSet) {
        return "episen-e-commerce".equalsIgnoreCase(claimsSet.getIssuer());
    }

    /**
     * Transforme le token JWT en objet User.
     * @param token Le token JWT.
     * @return L'objet User extrait du token.
     */
    public User transform(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(jwsVerifier)) {
                throw new RuntimeException("Invalid signature");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            if (!validateTokenExpiration(claimsSet)) {
                throw new JwtExpiredException("Token expired");
            }

            if (!validateTokenIssuer(claimsSet) || !validateTokenAudience(claimsSet)) {
                throw new RuntimeException("Invalid claims");
            }

            User user = new User();
            user.setUserId(claimsSet.getLongClaim("UserId"));
            user.setEmail(claimsSet.getStringClaim("Email"));
            user.setRoles(claimsSet.getStringListClaim("Roles"));
            return user;
        } catch (JwtExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid Token", e);
        }
    }
}
