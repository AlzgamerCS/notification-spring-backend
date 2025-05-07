package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.model.UserRole;
import com.nurtore.notification_spring.repository.UserRepository;
import com.nurtore.notification_spring.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        log.debug("Creating user with email: {}", user.getEmail());
        log.debug("Password present in createUser: {}", user.getPlainPassword() != null);
        log.debug("Password length in createUser: {}", user.getPlainPassword() != null ? user.getPlainPassword().length() : 0);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        validatePassword(user.getPlainPassword());
        String hashedPassword = passwordEncoder.encode(user.getPlainPassword());
        user.setPasswordHash(hashedPassword);
        // Don't clear the plain password yet as it might be needed for the response
        User savedUser = userRepository.save(user);
        
        // Clear the plain text password after saving
        savedUser.setPlainPassword(null);
        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user.getId()));
        
        // Update non-sensitive fields if provided
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getLastLoginAt() != null) {
            existingUser.setLastLoginAt(user.getLastLoginAt());
        }
        
        // Handle password update separately and explicitly
        if (user.getPlainPassword() != null && !user.getPlainPassword().isEmpty()) {
            validatePassword(user.getPlainPassword());
            existingUser.setPasswordHash(passwordEncoder.encode(user.getPlainPassword()));
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validatePassword(String password) {
        log.debug("Validating password, password present: {}", password != null);
        log.debug("Password length in validation: {}", password != null ? password.length() : 0);

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // boolean hasUppercase = false;
        // boolean hasLowercase = false;
        // boolean hasDigit = false;
        // boolean hasSpecial = false;
        // int length = password.length();
        // String specialChars = "!@#$%&?.,";

        // // Check each character once
        // for (char c : password.toCharArray()) {
        // if (Character.isUpperCase(c))
        // hasUppercase = true;
        // else if (Character.isLowerCase(c))
        // hasLowercase = true;
        // else if (Character.isDigit(c))
        // hasDigit = true;
        // else if (specialChars.indexOf(c) != -1)
        // hasSpecial = true;
        // }

        // // Check all conditions; throw a single exception if any fail
        // if (length < 8 || !hasUppercase || !hasLowercase || !hasDigit || !hasSpecial)
        // {
        // throw new IllegalArgumentException(
        // "Password must have at least 8 characters and contain an uppercase letter, "
        // +
        // "a lowercase letter, a digit, and a special character \"" + specialChars +
        // "\"");
        // }
    }
}