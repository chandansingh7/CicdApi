package com.pos.dto.request;

import com.pos.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 500, message = "Delivery address must be at most 500 characters")
    private String deliveryAddress;

    @NotNull(message = "Role is required")
    private Role role;

    private boolean active;
}
