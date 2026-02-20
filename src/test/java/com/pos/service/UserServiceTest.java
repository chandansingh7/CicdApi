package com.pos.service;

import com.pos.dto.request.AdminUpdateUserRequest;
import com.pos.dto.request.UpdateProfileRequest;
import com.pos.dto.response.UserResponse;
import com.pos.entity.User;
import com.pos.enums.Role;
import com.pos.exception.BadRequestException;
import com.pos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    private User admin;
    private User cashier;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).username("admin").email("admin@pos.com")
                .role(Role.ADMIN).active(true).build();
        cashier = User.builder().id(2L).username("cashier").email("cashier@pos.com")
                .role(Role.CASHIER).active(true).build();
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers returns all users mapped to DTOs")
    void getAllUsers_returnsMappedList() {
        when(userRepository.findAll()).thenReturn(List.of(admin, cashier));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponse::getUsername)
                .containsExactlyInAnyOrder("admin", "cashier");
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProfile returns correct user data")
    void getProfile_success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserResponse result = userService.getProfile("admin");

        assertThat(result.getEmail()).isEqualTo("admin@pos.com");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("getProfile throws when user not found")
    void getProfile_userNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("ghost"))
                .isInstanceOf(BadRequestException.class);
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile saves all profile fields successfully")
    void updateProfile_success() {
        when(userRepository.findByUsername("cashier")).thenReturn(Optional.of(cashier));
        when(userRepository.existsByEmail("new@pos.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("new@pos.com");
        req.setPhone("+1234567890");
        req.setAddress("123 Main St");
        req.setDeliveryAddress("456 Oak Ave");

        UserResponse result = userService.updateProfile("cashier", req);

        assertThat(result.getEmail()).isEqualTo("new@pos.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        assertThat(result.getAddress()).isEqualTo("123 Main St");
        assertThat(result.getDeliveryAddress()).isEqualTo("456 Oak Ave");
    }

    @Test
    @DisplayName("updateProfile keeps same email without uniqueness error")
    void updateProfile_sameEmail_noError() {
        when(userRepository.findByUsername("cashier")).thenReturn(Optional.of(cashier));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("cashier@pos.com"); // same email
        req.setFirstName("Jane");

        UserResponse result = userService.updateProfile("cashier", req);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        verify(userRepository, never()).existsByEmail("cashier@pos.com");
    }

    @Test
    @DisplayName("updateProfile throws when email already taken")
    void updateProfile_emailTaken() {
        when(userRepository.findByUsername("cashier")).thenReturn(Optional.of(cashier));
        when(userRepository.existsByEmail("taken@pos.com")).thenReturn(true);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setEmail("taken@pos.com");

        assertThatThrownBy(() -> userService.updateProfile("cashier", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    // ── adminUpdateUser ───────────────────────────────────────────────────────

    @Test
    @DisplayName("adminUpdateUser updates all fields including role and active flag")
    void adminUpdateUser_success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(cashier));
        when(userRepository.existsByEmail("updated@pos.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUpdateUserRequest req = new AdminUpdateUserRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("updated@pos.com");
        req.setPhone("+9876543210");
        req.setAddress("789 Pine Rd");
        req.setDeliveryAddress("101 Elm Blvd");
        req.setRole(Role.MANAGER);
        req.setActive(true);

        UserResponse result = userService.adminUpdateUser(2L, req, "admin");

        assertThat(result.getEmail()).isEqualTo("updated@pos.com");
        assertThat(result.getRole()).isEqualTo(Role.MANAGER);
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getPhone()).isEqualTo("+9876543210");
        assertThat(result.getDeliveryAddress()).isEqualTo("101 Elm Blvd");
    }

    @Test
    @DisplayName("adminUpdateUser throws when admin tries to deactivate own account")
    void adminUpdateUser_cannotDeactivateSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        AdminUpdateUserRequest req = new AdminUpdateUserRequest();
        req.setEmail("admin@pos.com");
        req.setRole(Role.ADMIN);
        req.setActive(false);

        assertThatThrownBy(() -> userService.adminUpdateUser(1L, req, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot deactivate your own account");

        verify(userRepository, never()).save(any());
    }

    // ── toggleActive ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("toggleActive deactivates an active user")
    void toggleActive_deactivates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(cashier));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.toggleActive(2L, "admin");

        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("toggleActive throws when admin tries to toggle own account")
    void toggleActive_cannotToggleSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.toggleActive(1L, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot deactivate your own account");

        verify(userRepository, never()).save(any());
    }
}
