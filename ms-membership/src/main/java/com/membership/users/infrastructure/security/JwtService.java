package com.membership.users.infrastructure.security;

import com.membership.users.domain.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final KeyPair keyPair;
    private final long expirationSeconds = Duration.ofHours(1).toSeconds();

    public JwtService(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        List<String> roles = parseRoles(user.getRoles());

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String getPublicKeyPem() {
        String base64Key = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(keyPair.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64Key + "\n-----END PUBLIC KEY-----";
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private List<String> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return List.of();
        }
        return List.of(roles.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
