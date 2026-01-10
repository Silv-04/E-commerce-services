package com.membership.users.infrastructure.security;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class InfraSetting {

    public static KeyPair keyPairLoader() {
        try {
            // Charger le keystore PKCS12
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream("/app/keys/server.p12");
            keyStore.load(fis, "jil".toCharArray());
            fis.close();

            // Récupérer la clé privée
            java.security.PrivateKey privateKey = (java.security.PrivateKey) 
                keyStore.getKey("jil", "jil".toCharArray());

            // Récupérer le certificat et la clé publique
            X509Certificate certificate = (X509Certificate) 
                keyStore.getCertificate("jil");
            java.security.PublicKey publicKey = certificate.getPublicKey();

            return new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement du keystore RSA: " + e.getMessage(), e);
        }
    }
}
