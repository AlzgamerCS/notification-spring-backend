package com.nurtore.notification_spring.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateEventRequest {
    private String summary;
    private String location;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String timeZone;
    private List<String> attendeeEmails;
} 