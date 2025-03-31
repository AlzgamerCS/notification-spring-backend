package com.nurtore.notification_spring.service;

import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.model.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(User user);
    User updateUser(User user);
    Optional<User> getUserById(UUID id);
    Optional<User> getUserByEmail(String email);
    List<User> getUsersByRole(UserRole role);
    List<User> getAllUsers();
    void deleteUser(UUID id);
    boolean existsByEmail(String email);
}