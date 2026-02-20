package com.pos.service;

import com.pos.dto.request.ChangePasswordRequest;
import com.pos.entity.User;
import com.pos.enums.Role;
import com.pos.exception.BadRequestException;
import com.pos.repository.UserRepository;
import com.pos.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService – changePassword")
class AuthServiceChangePasswordTest {

    @Mock UserRepository     userRepository;
    @Mock PasswordEncoder    passwordEncoder;
    @Mock JwtTokenProvider   jwtTokenProvider;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    private User user;
    private static final String USERNAME        = "testuser";
    private static final String ENCODED_CURRENT = "$2a$10$encodedCurrent";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username(USERNAME)
                .email("test@example.com")
                .password(ENCODED_CURRENT)
                .role(Role.CASHIER)
                .active(true)
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ChangePasswordRequest req(String current, String next, String confirm) {
        ChangePasswordRequest r = new ChangePasswordRequest();
        r.setCurrentPassword(current);
        r.setNewPassword(next);
        r.setConfirmPassword(confirm);
        return r;
    }

    // ── success path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updates password when all inputs are valid")
    void changePassword_success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", ENCODED_CURRENT)).thenReturn(true);
        when(passwordEncoder.matches("NewPass1", ENCODED_CURRENT)).thenReturn(false);
        when(passwordEncoder.encode("NewPass1")).thenReturn("$2a$10$encodedNew");

        assertThatNoException()
                .isThrownBy(() -> authService.changePassword(USERNAME, req("OldPass1", "NewPass1", "NewPass1")));

        verify(userRepository).save(argThat(u -> u.getPassword().equals("$2a$10$encodedNew")));
    }

    // ── failure paths ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("throws when user does not exist")
    void changePassword_userNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword("ghost", req("any", "New1", "New1")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when current password is wrong")
    void changePassword_wrongCurrentPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", ENCODED_CURRENT)).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(USERNAME, req("WrongPass", "New1", "New1")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when new password and confirm do not match")
    void changePassword_confirmMismatch() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", ENCODED_CURRENT)).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(USERNAME, req("OldPass1", "NewPass1", "Different")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when new password is the same as current password")
    void changePassword_sameAsCurrentPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", ENCODED_CURRENT)).thenReturn(true);
        when(passwordEncoder.matches("OldPass1", ENCODED_CURRENT)).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(USERNAME, req("OldPass1", "OldPass1", "OldPass1")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("different from the current");

        verify(userRepository, never()).save(any());
    }
}
