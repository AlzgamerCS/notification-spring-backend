package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.model.UserRole;
import com.nurtore.notification_spring.security.JwtService;
import com.nurtore.notification_spring.service.UserService;
import com.nurtore.notification_spring.service.EmailVerificationService;
import com.nurtore.notification_spring.dto.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class VerifyEmailRequest {
        @NotBlank(message = "Token is required")
        private String token;
    }

    @Data
    public static class ResendVerificationRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @Data
    public static class AuthResponse {
        private final String token;
        private final User user;
        private final String name;
        private final String email;
        private final String role;
        private final LocalDateTime lastLoginAt;
        private final boolean emailVerified;

        public AuthResponse(String token, User user) {
            this.token = token;
            this.user = user;
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.lastLoginAt = user.getLastLoginAt();
            this.emailVerified = user.isEmailVerified();
        }
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            // First try to get the user to check verification status
            User user = userService.getUserByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Check email verification before attempting authentication
            if (!user.isEmailVerified()) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "EMAIL_NOT_VERIFIED");
                response.put("message", "Please verify your email before logging in");
                response.put("email", request.getEmail());
                return ResponseEntity.status(403).body(response);
            }

            // Proceed with authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            
            // Update last login
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setLastLoginAt(LocalDateTime.now());
            user = userService.updateUser(updateUser);

            return ResponseEntity.ok(new AuthResponse(token, user));
        } catch (BadCredentialsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "INVALID_CREDENTIALS");
            response.put("message", "Invalid email or password");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            log.debug("Received registration request for email: {}", request.getEmail());
            log.debug("Password present: {}", request.getPassword() != null);
            log.debug("Password length: {}", request.getPassword() != null ? request.getPassword().length() : 0);

            User newUser = new User();
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPlainPassword(request.getPassword());
            newUser.setRole(UserRole.USER);
            newUser.setEmailVerified(false);

            log.debug("Created user object with password present: {}", newUser.getPlainPassword() != null);
            log.debug("User password length: {}", newUser.getPlainPassword() != null ? newUser.getPlainPassword().length() : 0);

            User createdUser = userService.createUser(newUser);
            
            // Send verification email
            emailVerificationService.sendVerificationEmail(createdUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful. Please check your email for verification.");
            response.put("email", createdUser.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Registration failed", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "REGISTRATION_FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        try {
            boolean verified = emailVerificationService.verifyEmail(request.getToken());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "VERIFICATION_FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody @Valid ResendVerificationRequest request) {
        try {
            emailVerificationService.resendVerificationEmail(request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification email sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "RESEND_FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }
}