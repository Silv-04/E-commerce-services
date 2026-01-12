package com.ecommerce.order.security;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.ecommerce.order.domain.entity.User;
import com.ecommerce.order.settings.InfraSetting;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.PostConstruct;

@Service
public class JwtTokenValidator {

    private JWSVerifier jwsVerifier;

    @PostConstruct
    public void init() {
        try {
            jwsVerifier = new RSASSAVerifier((java.security.interfaces.RSAPublicKey) InfraSetting.loadPublicKey());
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'initialiser le JwtTokenValidator", e);
        }
    }

    private boolean validateTokenAudience(JWTClaimsSet claimsSet) {
        return claimsSet.getAudience().contains("web");
    }

    private boolean validateTokenExpiration(JWTClaimsSet claimsSet) {
        return Instant.now().isBefore(claimsSet.getExpirationTime().toInstant());
    }

    private boolean validateTokenIssuer(JWTClaimsSet claimsSet) {
        return "episen-e-commerce".equalsIgnoreCase(claimsSet.getIssuer());
    }

    private boolean validate(JWTClaimsSet claimsSet) {
        return validateTokenIssuer(claimsSet) && validateTokenAudience(claimsSet);
    }

    /**
     * Transforme un token JWT en User
     * 
     * @param token JWT
     * @return User
     * @throws TokenExpiredException si token expiré
     * @throws RuntimeException si token invalide
     */
    public User transform(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!signedJWT.verify(jwsVerifier)) {
                throw new RuntimeException("Token invalide : signature non vérifiée");
            }

            if (!validateTokenExpiration(claims)) {
                throw new TokenExpiredException("Token expiré");
            }

            if (!validate(claims)) {
                throw new RuntimeException("Token invalide : audience ou issuer incorrect");
            }

            User user = new User();
            user.setUsername(claims.getSubject());
            user.setId(claims.getLongClaim("userId"));
            user.setEmail(claims.getStringClaim("email"));
            user.setRoles(claims.getStringListClaim("roles"));

            return user;
        } catch (TokenExpiredException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du token JWT", e);
        }
    }

    // Exception spécifique pour token expiré
    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) {
            super(message);
        }
    }
}
