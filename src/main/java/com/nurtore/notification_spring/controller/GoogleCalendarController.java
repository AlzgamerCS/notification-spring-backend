package com.nurtore.notification_spring.controller;

import com.nurtore.notification_spring.service.GoogleCalendarService;
import com.nurtore.notification_spring.dto.CreateEventRequest;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @GetMapping("/events")
    public List<Event> getUpcomingEvents() throws IOException {
        return googleCalendarService.getUpcomingEvents();
    }

    @PostMapping("/events")
    public Event createEvent(@RequestBody CreateEventRequest request) throws IOException {
        return googleCalendarService.createEvent(
            request.getSummary(),
            request.getLocation(),
            request.getDescription(),
            request.getStartDateTime(),
            request.getEndDateTime(),
            request.getTimeZone(),
            request.getAttendeeEmails()
        );
    }
} 