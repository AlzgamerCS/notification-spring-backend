package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.dto.NotificationDTO;
import com.nurtore.notification_spring.dto.CreateNotificationRequest;
import com.nurtore.notification_spring.dto.CreateNotificationWithEventRequest;
import com.nurtore.notification_spring.dto.NotificationWithDocumentDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<NotificationDTO> createNotification(
            @Valid @RequestBody CreateNotificationRequest request,
            @AuthenticationPrincipal User user) {
        Document document = documentService.getDocumentById(request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + request.getDocumentId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setDocument(document);
        notification.setChannel(request.getChannel());
        notification.setType(request.getType());
        notification.setScheduledAt(request.getScheduledAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationDTO.fromEntity(notificationService.createNotification(notification)));
    }

    @PostMapping("/with-event")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationDTO> createNotificationWithEvent(
            @Valid @RequestBody CreateNotificationWithEventRequest request,
            @AuthenticationPrincipal User user) {
        Document document = documentService.getDocumentById(request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + request.getDocumentId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setDocument(document);
        notification.setChannel(request.getChannel());
        notification.setType(request.getType());
        notification.setScheduledAt(request.getScheduledAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationDTO.fromEntity(
                    notificationService.createNotificationWithEvent(notification, request.getCalendarEventDetails())
                ));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> scheduleNotification(@Valid @RequestBody Notification notification) {
        // Fetch the actual entities to avoid lazy loading issues
        User user = userService.getUserById(notification.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + notification.getUser().getId()));
        Document document = documentService.getDocumentById(notification.getDocument().getId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + notification.getDocument().getId()));
        
        notificationService.scheduleNotification(document, user, notification.getType(), notification.getScheduledAt());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateNotification(
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
        
        notificationService.updateNotification(notification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @notificationPermissionEvaluator.hasAccess(#id, authentication.principal)")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable UUID id) {
        return notificationService.getNotificationById(id)
                .map(notification -> ResponseEntity.ok(NotificationDTO.fromEntity(notification)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                    notificationService.getNotificationsByUser(user).stream()
                        .map(NotificationDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<NotificationWithDocumentDTO>> getMyNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
            notificationService.getNotificationsByUser(user).stream()
                .map(NotificationWithDocumentDTO::fromEntity)
                .toList());
    }

    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @documentPermissionEvaluator.hasAccess(#documentId, authentication.principal)")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByDocument(@PathVariable UUID documentId) {
        return documentService.getDocumentById(documentId)
                .map(document -> ResponseEntity.ok(
                    notificationService.getNotificationsByDocument(document).stream()
                        .map(NotificationDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(
            notificationService.getNotificationsByStatus(status).stream()
                .map(NotificationDTO::fromEntity)
                .toList());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<NotificationDTO>> getPendingNotificationsDue() {
        return ResponseEntity.ok(
            notificationService.getPendingNotificationsDue().stream()
                .map(NotificationDTO::fromEntity)
                .toList());
    }

    @GetMapping("/user/{userId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<NotificationDTO>> getUserNotificationsByStatus(
            @PathVariable UUID userId,
            @PathVariable NotificationStatus status) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                    notificationService.getUserNotificationsByStatus(user, status).stream()
                        .map(NotificationDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<NotificationDTO>> getUserNotificationsInDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                    notificationService.getUserNotificationsInDateRange(user, startDate, endDate).stream()
                        .map(NotificationDTO::fromEntity)
                        .toList()))
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
    public ResponseEntity<NotificationDTO> markNotificationAsSent(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationDTO.fromEntity(notificationService.markNotificationAsSent(id)));
    }

    @PatchMapping("/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @notificationPermissionEvaluator.hasAccess(#id, authentication.principal)")
    public ResponseEntity<NotificationDTO> markNotificationAsDismissed(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationDTO.fromEntity(notificationService.markNotificationAsDismissed(id)));
    }

    @PostMapping("/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> processNotifications() {
        notificationService.processNotifications();
        return ResponseEntity.accepted().build();
    }
} 