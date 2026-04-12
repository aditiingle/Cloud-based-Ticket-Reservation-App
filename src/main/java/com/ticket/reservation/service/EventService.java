package com.ticket.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticket.reservation.dto.EventRequestDTO;
import com.ticket.reservation.dto.EventResponseDTO;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Ticket;
import com.ticket.reservation.repository.EventRepository;
import com.ticket.reservation.repository.ReservationRepository;
import com.ticket.reservation.repository.TicketRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    public EventService(EventRepository eventRepository, ReservationRepository reservationRepository, TicketRepository ticketRepository, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    public List<Event> searchEvents(String name) {
        return eventRepository.findByNameContainingIgnoreCaseAndIsCancelledFalse(name);
    }

    public List<Event> searchEventsByCategory(String category) {
        return eventRepository.findByCategoryIgnoreCaseAndIsCancelledFalse(category);
    }

    public List<Event> searchEventsByLocation(String location) {
        return eventRepository.findByLocationContainingIgnoreCaseAndIsCancelledFalse(location);
    }

    public List<Event> searchEventsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        return eventRepository.findByDateTimeBetweenAndIsCancelledFalse(startOfDay, endOfDay);
    }

    /**
     * Add a new event (Admin only).
     * @param event The event to add
     * @return The saved event
     */
    public Event addEvent(Event event) {
        event.setCancelled(false); // Ensure new events are not cancelled
        return eventRepository.save(event);
    }

    /**
     * Edit an existing event (Admin only).
     * Triggers notifications to all users with reservations for this event.
     * @param id The event ID
     * @param updatedEvent The updated event data
     * @return The updated event
     * @throws IllegalArgumentException if event not found
     */
    public Event editEvent(String id, Event updatedEvent) {
        Optional<Event> existingEventOpt = eventRepository.findById(id);
        
        if (existingEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found with id: " + id);
        }

        Event existingEvent = existingEventOpt.get();
        
        // Update event fields
        existingEvent.setName(updatedEvent.getName());
        existingEvent.setCategory(updatedEvent.getCategory());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setDateTime(updatedEvent.getDateTime());
        existingEvent.setPrice(updatedEvent.getPrice());
        
        // Save the updated event
        Event savedEvent = eventRepository.save(existingEvent);
        
        // Cascade logic: Find tickets by eventId, then get reservations and send notifications
        List<Ticket> tickets = ticketRepository.findByEventId(id);
        for (Ticket ticket : tickets) {
            reservationRepository.findById(ticket.getReservationId()).ifPresent(reservation -> {
                notificationService.sendEventUpdateNotification(reservation.getId(), savedEvent);
            });
        }
        
        return savedEvent;
    }

    /**
     * Cancel an event (Admin only).
     * Sets isCancelled to true and cancels all associated reservations.
     * Sends cancellation notifications to all affected users.
     * @param id The event ID
     * @return The cancelled event
     * @throws IllegalArgumentException if event not found
     */
    public Event cancelEvent(String id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        
        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found with id: " + id);
        }

        Event event = eventOpt.get();
        event.setCancelled(true);
        eventRepository.save(event);
        
        // Cascade logic: Find tickets by eventId, then get reservations and cancel them
        List<Ticket> tickets = ticketRepository.findByEventId(id);
        for (Ticket ticket : tickets) {
            reservationRepository.findById(ticket.getReservationId()).ifPresent(reservation -> {
                reservation.setStatus("CANCELLED");
                reservationRepository.save(reservation);
                // Send event cancelled notification to each reservation
                notificationService.sendEventCancelledNotification(reservation.getId(), event);
            });
        }
        
        return event;
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    // DTO-based methods for controller layer

    /**
     * Convert Event entity to EventResponseDTO.
     */
    public EventResponseDTO toResponseDTO(Event event) {
        return new EventResponseDTO(
            event.getId(),
            event.getName(),
            event.getCategory(),
            event.getDescription(),
            event.getLocation(),
            event.getDateTime(),
            event.getPrice(),
            event.isCancelled()
        );
    }

    /**
     * Convert EventRequestDTO to Event entity.
     */
    public Event toEntity(EventRequestDTO dto) {
        Event event = new Event();
        event.setName(dto.getName());
        event.setCategory(dto.getCategory());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setDateTime(dto.getDateTime());
        event.setPrice(dto.getPrice());
        return event;
    }

    /**
     * Add a new event using DTO (Admin only).
     */
    public EventResponseDTO addEventFromDTO(EventRequestDTO dto) {
        Event event = toEntity(dto);
        Event savedEvent = addEvent(event);
        return toResponseDTO(savedEvent);
    }

    /**
     * Edit an existing event using DTO (Admin only).
     */
    public EventResponseDTO editEventFromDTO(String id, EventRequestDTO dto) {
        Optional<Event> existingEventOpt = eventRepository.findById(id);
        
        if (existingEventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event not found with id: " + id);
        }

        Event existingEvent = existingEventOpt.get();
        
        // Update event fields from DTO
        existingEvent.setName(dto.getName());
        existingEvent.setCategory(dto.getCategory());
        existingEvent.setDescription(dto.getDescription());
        existingEvent.setLocation(dto.getLocation());
        existingEvent.setDateTime(dto.getDateTime());
        existingEvent.setPrice(dto.getPrice());
        
        // Save the updated event
        Event savedEvent = eventRepository.save(existingEvent);
        
        // Cascade logic: Find tickets by eventId, then get reservations and send notifications
        List<Ticket> tickets = ticketRepository.findByEventId(id);
        for (Ticket ticket : tickets) {
            reservationRepository.findById(ticket.getReservationId()).ifPresent(reservation -> {
                notificationService.sendEventUpdateNotification(reservation.getId(), savedEvent);
            });
        }
        
        return toResponseDTO(savedEvent);
    }

    /**
     * Cancel an event and return DTO (Admin only).
     */
    public EventResponseDTO cancelEventAndReturnDTO(String id) {
        Event cancelledEvent = cancelEvent(id);
        return toResponseDTO(cancelledEvent);
    }
}
