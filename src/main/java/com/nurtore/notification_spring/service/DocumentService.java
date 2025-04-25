package com.nurtore.notification_spring.service;

import com.nurtore.notification_spring.model.Document;
import com.nurtore.notification_spring.model.DocumentCategory;
import com.nurtore.notification_spring.model.DocumentStatus;
import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.dto.CalendarEventDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentService {
    Document createDocument(Document document);
    Document createDocument(Document document, CalendarEventDetails calendarEventDetails);
    Document updateDocument(Document document);
    Optional<Document> getDocumentById(UUID id);
    List<Document> getDocumentsByOwner(User owner);
    List<Document> getDocumentsByStatus(DocumentStatus status);
    List<Document> getDocumentsByCategory(DocumentCategory category);
    List<Document> getExpiringDocuments(LocalDate expirationDate);
    List<Document> getDocumentsExpiringBetween(LocalDate startDate, LocalDate endDate);
    List<Document> searchDocumentsByTitle(String titlePart);
    void deleteDocument(UUID id);
    Document updateDocumentStatus(UUID id, DocumentStatus status);
} 