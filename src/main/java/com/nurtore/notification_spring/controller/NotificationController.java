package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.model.*;
import com.nurtore.notification_spring.service.DocumentService;
import com.nurtore.notification_spring.service.NotificationService;
import com.nurtore.notification_spring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;
    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(notification));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Notification> scheduleNotification(
            @RequestParam UUID documentId,
            @RequestParam UUID userId,
            @RequestParam NotificationType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt) {
        
        Document document = documentService.getDocumentById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.scheduleNotification(document, user, type, scheduledAt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable UUID id,
            @Valid @RequestBody Notification notification) {
        notification.setId(id);
        
        // Fetch the actual entities to avoid lazy loading issues
        User user = userService.getUserById(notification.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + notification.getUser().getId()));
        Document document = documentService.getDocumentById(notification.getDocument().getId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + notification.getDocument().getId()));
        
        notification.setUser(user);
        notification.setDocument(document);
        
        return ResponseEntity.ok(notificationService.updateNotification(notification));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @notificationPermissionEvaluator.hasAccess(#id, authentication.principal)")
    public ResponseEntity<Notification> getNotificationById(@PathVariable UUID id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(notificationService.getNotificationsByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @documentPermissionEvaluator.hasAccess(#documentId, authentication.principal)")
    public ResponseEntity<List<Notification>> getNotificationsByDocument(@PathVariable UUID documentId) {
        return documentService.getDocumentById(documentId)
                .map(document -> ResponseEntity.ok(notificationService.getNotificationsByDocument(document)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.getNotificationsByStatus(status));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Notification>> getPendingNotificationsDue() {
        return ResponseEntity.ok(notificationService.getPendingNotificationsDue());
    }

    @GetMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<Notification>> getUserNotificationsByStatus(
            @PathVariable UUID userId,
            @PathVariable NotificationStatus status) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(notificationService.getUserNotificationsByStatus(user, status)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<Notification>> getUserNotificationsInDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                        notificationService.getUserNotificationsInDateRange(user, startDate, endDate)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/mark-sent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Notification> markNotificationAsSent(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markNotificationAsSent(id));
    }

    @PatchMapping("/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @notificationPermissionEvaluator.hasAccess(#id, authentication.principal)")
    public ResponseEntity<Notification> markNotificationAsDismissed(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markNotificationAsDismissed(id));
    }

    @PostMapping("/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> processNotifications() {
        notificationService.processNotifications();
        return ResponseEntity.accepted().build();
    }
} 