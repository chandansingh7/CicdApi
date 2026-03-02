package com.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CloseShiftRequest {

    @NotNull(message = "Counted cash is required")
    @DecimalMin(value = "0.00", message = "Counted cash cannot be negative")
    private BigDecimal countedCash;
}

