package com.episen.ms_product.security;

/**
 * Exception levée lorsque le token JWT est expiré.
 */
public class JwtExpiredException extends RuntimeException {
    public JwtExpiredException(String message) {
        super(message);
    }
}
