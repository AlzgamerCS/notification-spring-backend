package com.nurtore.notification_spring.service.impl;

import com.nurtore.notification_spring.model.Document;
import com.nurtore.notification_spring.model.DocumentCategory;
import com.nurtore.notification_spring.model.DocumentStatus;
import com.nurtore.notification_spring.model.User;
import com.nurtore.notification_spring.repository.DocumentRepository;
import com.nurtore.notification_spring.service.DocumentService;
import com.nurtore.notification_spring.service.GoogleCalendarService;
import com.nurtore.notification_spring.dto.CalendarEventDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final GoogleCalendarService googleCalendarService;

    @Override
    public Document createDocument(Document document) {
        return createDocument(document, null);
    }

    @Override
    public Document createDocument(Document document, CalendarEventDetails calendarEventDetails) {
        // Set initial status if not set
        if (document.getStatus() == null) {
            document.setStatus(DocumentStatus.ACTIVE);
        }
        
        Document savedDocument = documentRepository.save(document);

        // Create calendar event if requested
        if (calendarEventDetails != null && calendarEventDetails.isCreateCalendarEvent()) {
            try {
                // Create list of attendees with the user's email
                List<String> attendees = List.of(document.getOwner().getEmail());

                // Create calendar event
                googleCalendarService.createEvent(
                    calendarEventDetails.getSummary() != null ? calendarEventDetails.getSummary() : document.getTitle(),
                    "", // location
                    calendarEventDetails.getDescription() != null ? calendarEventDetails.getDescription() : document.getDescription(),
                    calendarEventDetails.getStartDateTime(),
                    calendarEventDetails.getEndDateTime(),
                    calendarEventDetails.getTimeZone() != null ? calendarEventDetails.getTimeZone() : "UTC",
                    attendees
                );
            } catch (Exception e) {
                // Log the error but don't fail the document creation
                System.err.println("Failed to create calendar event: " + e.getMessage());
            }
        }

        return savedDocument;
    }

    @Override
    public Document updateDocument(Document document) {
        if (!documentRepository.existsById(document.getId())) {
            throw new EntityNotFoundException("Document not found with id: " + document.getId());
        }
        return documentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(UUID id) {
        return documentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByOwner(User owner) {
        return documentRepository.findByOwner(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByStatus(DocumentStatus status) {
        return documentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByCategory(DocumentCategory category) {
        return documentRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getExpiringDocuments(LocalDate expirationDate) {
        return documentRepository.findExpiringDocuments(expirationDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentsExpiringBetween(LocalDate startDate, LocalDate endDate) {
        return documentRepository.findDocumentsExpiringBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> searchDocumentsByTitle(String titlePart) {
        return documentRepository.findByTitleContainingIgnoreCase(titlePart);
    }

    @Override
    public void deleteDocument(UUID id) {
        if (!documentRepository.existsById(id)) {
            throw new EntityNotFoundException("Document not found with id: " + id);
        }
        documentRepository.deleteById(id);
    }

    @Override
    public Document updateDocumentStatus(UUID id, DocumentStatus status) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));
        document.setStatus(status);
        return documentRepository.save(document);
    }
} 