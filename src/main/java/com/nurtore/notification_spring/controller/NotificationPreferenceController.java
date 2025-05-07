package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.dto.NotificationPreferenceDTO;
import com.nurtore.notification_spring.model.NotificationChannel;
import com.nurtore.notification_spring.model.NotificationPreference;
import com.nurtore.notification_spring.service.NotificationPreferenceService;
import com.nurtore.notification_spring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {
    private final NotificationPreferenceService preferenceService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<NotificationPreferenceDTO> createPreference(@Valid @RequestBody NotificationPreference preference) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationPreferenceDTO.fromEntity(preferenceService.createPreference(preference)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationPreferenceDTO> updatePreference(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationPreference preference) {
        preference.setId(id);
        return ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preferenceService.updatePreference(preference)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationPreferenceDTO> getPreferenceById(@PathVariable UUID id) {
        return preferenceService.getPreferenceById(id)
                .map(preference -> ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preference)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationPreferenceDTO>> getPreferencesByUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                    preferenceService.getPreferencesByUser(user).stream()
                        .map(NotificationPreferenceDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/channel/{channel}")
    public ResponseEntity<NotificationPreferenceDTO> getPreferenceByUserAndChannel(
            @PathVariable UUID userId,
            @PathVariable NotificationChannel channel) {
        return userService.getUserById(userId)
                .flatMap(user -> preferenceService.getPreferenceByUserAndChannel(user, channel))
                .map(preference -> ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preference)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/enabled")
    public ResponseEntity<List<NotificationPreferenceDTO>> getEnabledPreferencesByUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                    preferenceService.getEnabledPreferencesByUser(user).stream()
                        .map(NotificationPreferenceDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreference(@PathVariable UUID id) {
        preferenceService.deletePreference(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<NotificationPreferenceDTO> togglePreference(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(NotificationPreferenceDTO.fromEntity(preferenceService.togglePreference(id, enabled)));
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<NotificationPreferenceDTO>> getPreferencesByChannel(
            @PathVariable NotificationChannel channel) {
        return ResponseEntity.ok(
            preferenceService.getPreferencesByChannel(channel).stream()
                .map(NotificationPreferenceDTO::fromEntity)
                .toList());
    }
} 