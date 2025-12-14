package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.enumerate.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateDTO {

    @NotNull(message = "Le statut est obligatoire")
    private OrderStatus status;

    public StatusUpdateDTO() {}

    public StatusUpdateDTO(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
