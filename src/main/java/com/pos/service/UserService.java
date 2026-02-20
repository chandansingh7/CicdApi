package com.pos.service;

import com.pos.dto.request.AdminUpdateUserRequest;
import com.pos.dto.request.UpdateProfileRequest;
import com.pos.dto.response.UserResponse;
import com.pos.entity.User;
import com.pos.exception.BadRequestException;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getProfile(String username) {
        User user = findByUsername(username);
        return UserResponse.from(user);
    }

    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findByUsername(username);

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse adminUpdateUser(Long id, AdminUpdateUserRequest request, String currentUsername) {
        User user = findById(id);

        if (user.getUsername().equals(currentUsername) && !request.isActive()) {
            throw new BadRequestException("You cannot deactivate your own account");
        }
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(request.isActive());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse toggleActive(Long id, String currentUsername) {
        User user = findById(id);

        if (user.getUsername().equals(currentUsername)) {
            throw new BadRequestException("You cannot deactivate your own account");
        }

        user.setActive(!user.isActive());
        return UserResponse.from(userRepository.save(user));
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + id));
    }
}
