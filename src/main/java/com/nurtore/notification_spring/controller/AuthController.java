package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.model.UserRole;
import com.nurtore.notification_spring.security.JwtService;
import com.nurtore.notification_spring.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private final String token;
        private final User user;
        private final String name;
        private final String email;
        private final String role;
        private final LocalDateTime lastLoginAt;

        public AuthResponse(String token, User user) {
            this.token = token;
            this.user = user;
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.lastLoginAt = user.getLastLoginAt();
        }
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        // First authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // Get user details and generate token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        
        // Get the full user entity
        User user = userService.getUserByEmail(userDetails.getUsername()).orElseThrow();
        
        // Create a new user object for the update to avoid modifying the loaded entity directly
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginAt(LocalDateTime.now());
        
        // Update only the lastLoginAt timestamp
        user = userService.updateUser(updateUser);

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword());
        newUser.setRole(UserRole.USER);

        User createdUser = userService.createUser(newUser);
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(createdUser.getEmail())
                        .password(createdUser.getPasswordHash())
                        .authorities("ROLE_" + createdUser.getRole().name())
                        .build());

        return ResponseEntity.ok(new AuthResponse(token, createdUser));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }
}