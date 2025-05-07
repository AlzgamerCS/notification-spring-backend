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
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable UUID id) {
        return notificationService.getNotificationById(id)
                .map(notification -> ResponseEntity.ok(NotificationDTO.fromEntity(notification)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(
                    notificationService.getNotificationsByUser(user).stream()
                        .map(NotificationDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationWithDocumentDTO>> getMyNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
            notificationService.getNotificationsByUser(user).stream()
                .map(NotificationWithDocumentDTO::fromEntity)
                .toList());
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByDocument(@PathVariable UUID documentId) {
        return documentService.getDocumentById(documentId)
                .map(document -> ResponseEntity.ok(
                    notificationService.getNotificationsByDocument(document).stream()
                        .map(NotificationDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(
            notificationService.getNotificationsByStatus(status).stream()
                .map(NotificationDTO::fromEntity)
                .toList());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<NotificationDTO>> getPendingNotificationsDue() {
        return ResponseEntity.ok(
            notificationService.getPendingNotificationsDue().stream()
                .map(NotificationDTO::fromEntity)
                .toList());
    }

    @GetMapping("/user/{userId}/status/{status}")
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
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/mark-sent")
    public ResponseEntity<NotificationDTO> markNotificationAsSent(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationDTO.fromEntity(notificationService.markNotificationAsSent(id)));
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<NotificationDTO> markNotificationAsDismissed(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationDTO.fromEntity(notificationService.markNotificationAsDismissed(id)));
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processNotifications() {
        notificationService.processNotifications();
        return ResponseEntity.accepted().build();
    }
} 