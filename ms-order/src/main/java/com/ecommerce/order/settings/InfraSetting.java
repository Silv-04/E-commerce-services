package com.ecommerce.order.settings;

import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Paramètres d'infrastructure
 */
public class InfraSetting {
    public static PublicKey loadPublicKey() {
        try (FileInputStream fis = new FileInputStream("/app/keys/public_cert.pem")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
            return cert.getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger la clé publique", e);
        }
    }
}
