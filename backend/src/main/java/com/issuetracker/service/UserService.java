package com.issuetracker.service;

import com.issuetracker.dto.RegisterRequest;
import com.issuetracker.model.User;
import com.issuetracker.model.enums.Role;
import com.issuetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private Environment environment;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // In dev mode: auto-activate so you can log in without an email server
        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        if (isDev) {
            user.setActive(true);
            user.setActivationToken(null);
        } else {
            user.setActivationToken(UUID.randomUUID().toString());
            user.setActive(false);
        }

        User saved = userRepository.save(user);

        if (!isDev) {
            emailService.sendActivationEmail(saved.getEmail(), saved.getName(), saved.getActivationToken());
        }

        return saved;
    }

    public void activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));
        if (user.isActive()) throw new RuntimeException("Account is already activated");

        user.setActive(true);
        user.setActivationToken(null);
        userRepository.save(user);
    }

    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Admin-only: change a user's role
    public User updateRole(Long userId, Role role) {
        User user = findById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }
}
