package com.membership.users.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

@Configuration
public class JwtConfig {

    private static final String PRIVATE_KEY_PATH = "/app/keys/private_key.pem";
    private static final String PUBLIC_KEY_PATH = "/app/keys/public_cert.pem";

    @Bean
    public KeyPair rsaKeyPair() {
        try {
            PrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);
            PublicKey publicKey = loadPublicKey(PUBLIC_KEY_PATH);
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger les clés RSA depuis /app/keys", e);
        }
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String key = Files.readString(Path.of(path))
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = java.util.Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        byte[] certBytes = Files.readAllBytes(Path.of(path));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new java.io.ByteArrayInputStream(certBytes));
        return cert.getPublicKey();
    }
}
