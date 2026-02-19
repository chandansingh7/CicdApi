package com.pos.dto.request;

import com.pos.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private Long customerId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private BigDecimal discount = BigDecimal.ZERO;
}
