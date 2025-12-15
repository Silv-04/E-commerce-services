package com.ecommerce.order.infrastructure.client;

import java.util.Optional;

public interface IUserClient {
    java.util.Optional<UserDTO> getUserById(Long userId);
    boolean isServiceAvailable();
}