package com.ticket.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.reservation.dto.EventRequestDTO;
import com.ticket.reservation.dto.EventResponseDTO;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/events")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves all events (authenticated users)")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves a specific event by ID (authenticated users)")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search events by name", description = "Searches events by name (authenticated users)")
    public List<Event> searchEvents(@RequestParam String name) {
        return eventService.searchEvents(name);
    }

    @GetMapping("/search/category")
    @Operation(summary = "Search events by category", description = "Searches events by category (authenticated users)")
    public List<Event> searchEventsByCategory(@RequestParam String category) {
        return eventService.searchEventsByCategory(category);
    }

    @GetMapping("/search/location")
    @Operation(summary = "Search events by location", description = "Searches events by location (authenticated users)")
    public List<Event> searchEventsByLocation(@RequestParam String location) {
        return eventService.searchEventsByLocation(location);
    }

    @GetMapping("/search/date")
    @Operation(summary = "Search events by date", description = "Searches events by date (authenticated users)")
    public List<Event> searchEventsByDate(@RequestParam int year,
            @RequestParam int month, @RequestParam int day) {
        return eventService.searchEventsByDate(LocalDate.of(year, month, day));
    }

    @PostMapping
    @Operation(summary = "Add a new event", description = "Creates a new event (Admin only)")
    public ResponseEntity<EventResponseDTO> addEvent(@RequestBody EventRequestDTO eventRequest) {
        EventResponseDTO savedEvent = eventService.addEventFromDTO(eventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit an event", description = "Updates an existing event and notifies affected users (Admin only)")
    public ResponseEntity<EventResponseDTO> editEvent(@PathVariable String id, @RequestBody EventRequestDTO eventRequest) {
        try {
            EventResponseDTO updatedEvent = eventService.editEventFromDTO(id, eventRequest);
            return ResponseEntity.ok(updatedEvent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an event", description = "Cancels an event and all associated reservations, notifies affected users (Admin only)")
    public ResponseEntity<EventResponseDTO> cancelEvent(@PathVariable String id) {
        try {
            EventResponseDTO cancelledEvent = eventService.cancelEventAndReturnDTO(id);
            return ResponseEntity.ok(cancelledEvent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an event (alternative)", description = "Alternative endpoint to cancel an event (Admin only)")
    public ResponseEntity<EventResponseDTO> deleteEvent(@PathVariable String id) {
        try {
            EventResponseDTO cancelledEvent = eventService.cancelEventAndReturnDTO(id);
            return ResponseEntity.ok(cancelledEvent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
