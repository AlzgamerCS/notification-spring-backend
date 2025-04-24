package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.dto.DocumentDTO;
import com.nurtore.notification_spring.model.Document;
import com.nurtore.notification_spring.model.DocumentCategory;
import com.nurtore.notification_spring.model.DocumentStatus;
import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.service.DocumentService;
import com.nurtore.notification_spring.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DocumentDTO> createDocument(
            @Valid @RequestBody Document document,
            @AuthenticationPrincipal User user) {
        document.setOwner(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DocumentDTO.fromEntity(documentService.createDocument(document)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @documentPermissionEvaluator.isOwner(#id, authentication.principal)")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable UUID id, @Valid @RequestBody Document document) {
        document.setId(id);
        return ResponseEntity.ok(DocumentDTO.fromEntity(documentService.updateDocument(document)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @documentPermissionEvaluator.hasAccess(#id, authentication.principal)")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable UUID id) {
        return documentService.getDocumentById(id)
                .map(document -> ResponseEntity.ok(DocumentDTO.fromEntity(document)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == authentication.principal.id")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByOwner(@PathVariable UUID ownerId) {
        return userService.getUserById(ownerId)
                .map(owner -> ResponseEntity.ok(
                    documentService.getDocumentsByOwner(owner).stream()
                        .map(DocumentDTO::fromEntity)
                        .toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByStatus(@PathVariable DocumentStatus status) {
        return ResponseEntity.ok(
            documentService.getDocumentsByStatus(status).stream()
                .map(DocumentDTO::fromEntity)
                .toList());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByCategory(@PathVariable DocumentCategory category) {
        return ResponseEntity.ok(
            documentService.getDocumentsByCategory(category).stream()
                .map(DocumentDTO::fromEntity)
                .toList());
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DocumentDTO>> getExpiringDocuments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {
        return ResponseEntity.ok(
            documentService.getExpiringDocuments(expirationDate).stream()
                .map(DocumentDTO::fromEntity)
                .toList());
    }

    @GetMapping("/expiring/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DocumentDTO>> getDocumentsExpiringBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
            documentService.getDocumentsExpiringBetween(startDate, endDate).stream()
                .map(DocumentDTO::fromEntity)
                .toList());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DocumentDTO>> searchDocumentsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(
            documentService.searchDocumentsByTitle(title).stream()
                .map(DocumentDTO::fromEntity)
                .toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @documentPermissionEvaluator.isOwner(#id, authentication.principal)")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @documentPermissionEvaluator.isOwner(#id, authentication.principal)")
    public ResponseEntity<DocumentDTO> updateDocumentStatus(
            @PathVariable UUID id,
            @RequestParam DocumentStatus status) {
        return ResponseEntity.ok(DocumentDTO.fromEntity(documentService.updateDocumentStatus(id, status)));
    }
} 