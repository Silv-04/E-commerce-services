package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.enumerate.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateDTO {

    @NotNull(message = "Le statut est obligatoire")
    private OrderStatus status;
}
