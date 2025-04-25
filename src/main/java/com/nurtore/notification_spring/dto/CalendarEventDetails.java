package com.nurtore.notification_spring.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class CalendarEventDetails {
    private boolean createCalendarEvent;
    
    @NotNull(message = "Summary is required when creating a calendar event")
    private String summary;
    
    private String description;
    
    @NotNull(message = "Start date/time is required when creating a calendar event")
    private String startDateTime;
    
    @NotNull(message = "End date/time is required when creating a calendar event")
    private String endDateTime;
    
    private String timeZone = "UTC"; // Default to UTC if not specified
} 