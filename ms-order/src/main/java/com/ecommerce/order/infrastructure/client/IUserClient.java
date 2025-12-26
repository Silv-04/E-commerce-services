package com.ecommerce.order.infrastructure.client;

public interface IUserClient {
    java.util.Optional<UserDTO> getUserById(Long userId);
    boolean isServiceAvailable();
}