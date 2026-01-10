package com.ecommerce.order.settings;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class InfraSetting {
    public static KeyPair keyPairLoader() {
        try (FileInputStream is = new FileInputStream("/app/keys/server.p12")){
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(is, "jil".toCharArray());

            Key key = keystore.getKey("jil", "jil".toCharArray());
            Certificate certificate = keystore.getCertificate("jil");

            return new KeyPair(certificate.getPublicKey(), (PrivateKey) key);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
