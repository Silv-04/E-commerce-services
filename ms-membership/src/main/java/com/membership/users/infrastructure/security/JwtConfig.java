package com.membership.users.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

@Configuration
public class JwtConfig {

    private static final String KEY_PASSWORD = "jil"; // Mot de passe pour déchiffrer la clé privée si elle est chiffrée

    @Bean
    public KeyPair rsaKeyPair() {
        try {
            // Charger la clé privée depuis le fichier PEM
            String privateKeyPath = "../Keys/rsa_private.pem";
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            
            // Charger la clé publique depuis le certificat
            String certPath = "../Keys/rsa_cert.pem";
            PublicKey publicKey = loadPublicKey(certPath);
            
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger les clés RSA depuis le dossier Keys", e);
        }
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String keyContent = new String(Files.readAllBytes(Paths.get(path)))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] decodedKey = java.util.Base64.getDecoder().decode(keyContent);
        
        try {
            // Essayer de charger la clé sans mot de passe d'abord
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            // Si cela échoue, essayer avec le mot de passe "jil"
            try {
                EncryptedPrivateKeyInfo encryptedKeyInfo = new EncryptedPrivateKeyInfo(decodedKey);
                Cipher cipher = Cipher.getInstance(encryptedKeyInfo.getAlgName());
                PBEKeySpec pbeKeySpec = new PBEKeySpec(KEY_PASSWORD.toCharArray());
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedKeyInfo.getAlgName());
                javax.crypto.SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
                cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedKeyInfo.getAlgParameters());
                byte[] decryptedKeyBytes = cipher.doFinal(encryptedKeyInfo.getEncryptedData());
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePrivate(spec);
            } catch (Exception ex) {
                throw e; // Lever l'erreur originale si le mot de passe ne fonctionne pas
            }
        }
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        byte[] certBytes = Files.readAllBytes(Paths.get(path));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                new java.io.ByteArrayInputStream(certBytes)
        );
        return certificate.getPublicKey();
    }
}
