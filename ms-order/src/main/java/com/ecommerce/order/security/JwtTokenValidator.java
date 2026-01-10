package com.ecommerce.order.security;

import java.security.interfaces.RSAPublicKey;
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
        jwsVerifier = new RSASSAVerifier((RSAPublicKey) InfraSetting.keyPairLoader().getPublic());
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
        return validateTokenExpiration(claimsSet) &&
                validateTokenIssuer(claimsSet) &&
                validateTokenAudience(claimsSet);
    }

    public User transform(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(jwsVerifier) || !validate(signedJWT.getJWTClaimsSet())) {
                throw new RuntimeException("Token cannot be verified. Invalid Token.");
            }

            User user = new User();
            user.setUsername(signedJWT.getJWTClaimsSet().getSubject());
            user.setId(signedJWT.getJWTClaimsSet().getLongClaim("UserId"));
            user.setEmail(signedJWT.getJWTClaimsSet().getStringClaim("Email"));
            user.setRoles(signedJWT.getJWTClaimsSet().getStringListClaim("Roles"));
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
