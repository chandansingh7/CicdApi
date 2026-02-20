package com.pos.service;

import com.pos.dto.request.AdminUpdateUserRequest;
import com.pos.dto.request.UpdateProfileRequest;
import com.pos.dto.response.UserResponse;
import com.pos.entity.User;
import com.pos.exception.BadRequestException;
import com.pos.exception.ErrorCode;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
        log.debug("Returned {} users", users.size());
        return users;
    }

    public UserResponse getProfile(String username) {
        log.debug("Fetching profile for user: {}", username);
        return UserResponse.from(findByUsername(username));
    }

    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        log.info("Profile update requested by user: {}", username);
        User user = findByUsername(username);

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            log.warn("[US003] Profile update failed — email in use: {}", request.getEmail());
            throw new BadRequestException(ErrorCode.US003);
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDeliveryAddress(request.getDeliveryAddress());

        UserResponse saved = UserResponse.from(userRepository.save(user));
        log.info("Profile updated for user: {}", username);
        return saved;
    }

    public UserResponse adminUpdateUser(Long id, AdminUpdateUserRequest request, String currentUsername) {
        log.info("Admin '{}' updating user id: {}", currentUsername, id);
        User user = findById(id);

        if (user.getUsername().equals(currentUsername) && !request.isActive()) {
            log.warn("[US004] Admin update rejected — cannot deactivate self: {}", currentUsername);
            throw new BadRequestException(ErrorCode.US004);
        }
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            log.warn("[US003] Admin update failed — email in use: {}", request.getEmail());
            throw new BadRequestException(ErrorCode.US003);
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDeliveryAddress(request.getDeliveryAddress());
        user.setRole(request.getRole());
        user.setActive(request.isActive());

        UserResponse saved = UserResponse.from(userRepository.save(user));
        log.info("Admin updated user id: {} — role: {}, active: {}", id, request.getRole(), request.isActive());
        return saved;
    }

    public UserResponse toggleActive(Long id, String currentUsername) {
        log.info("Admin '{}' toggling active for user id: {}", currentUsername, id);
        User user = findById(id);

        if (user.getUsername().equals(currentUsername)) {
            log.warn("[US004] Toggle active rejected — cannot deactivate self: {}", currentUsername);
            throw new BadRequestException(ErrorCode.US004);
        }

        boolean newStatus = !user.isActive();
        user.setActive(newStatus);
        UserResponse saved = UserResponse.from(userRepository.save(user));
        log.info("User id: {} active set to: {}", id, newStatus);
        return saved;
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException(ErrorCode.US001));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.US001));
    }
}
