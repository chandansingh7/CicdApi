package com.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenShiftRequest {

    @NotNull(message = "Opening float is required")
    @DecimalMin(value = "0.00", message = "Opening float cannot be negative")
    private BigDecimal openingFloat;
}

