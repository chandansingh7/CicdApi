package com.pos.dto.response;

import com.pos.entity.User;
import com.pos.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long          id;
    private String        username;
    private String        firstName;
    private String        lastName;
    private String        email;
    private String        phone;
    private String        address;
    private String        deliveryAddress;
    private Role          role;
    private boolean       active;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .deliveryAddress(user.getDeliveryAddress())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
