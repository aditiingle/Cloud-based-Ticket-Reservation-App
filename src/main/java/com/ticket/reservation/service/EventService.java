package com.ticket.reservation.service;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> searchEvents(String name) {
        return eventRepository.findByNameContainingIgnoreCaseAndIsCancelledFalse(name);
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }
}