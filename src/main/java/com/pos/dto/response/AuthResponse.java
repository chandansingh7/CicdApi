package com.pos.dto.response;

import com.pos.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private String username;
    private String email;
    private Role role;
}
