package com.pos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank(message = "Customer name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
}
