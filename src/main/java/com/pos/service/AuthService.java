package com.pos.service;

import com.pos.dto.request.ChangePasswordRequest;
import com.pos.dto.request.LoginRequest;
import com.pos.dto.request.RegisterRequest;
import com.pos.dto.response.AuthResponse;
import com.pos.entity.User;
import com.pos.enums.Role;
import com.pos.exception.BadRequestException;
import com.pos.repository.UserRepository;
import com.pos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = (User) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        log.info("Login successful — user: {}, role: {}", user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}, role: {}",
                request.getUsername(), request.getRole());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed — username already taken: {}", request.getUsername());
            throw new BadRequestException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed — email already registered: {}", request.getEmail());
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.CASHIER;
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user);
        log.info("User registered successfully — username: {}, role: {}", user.getUsername(), role);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        log.info("Password change requested by user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed — incorrect current password for user: {}", username);
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed — confirmation mismatch for user: {}", username);
            throw new BadRequestException("New password and confirmation do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed — new password same as current for user: {}", username);
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
    }
}
