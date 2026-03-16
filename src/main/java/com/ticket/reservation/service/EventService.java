package com.ticket.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;

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

	public List<Event> searchEventsByCategory(String category) {
		return eventRepository.findByCategoryIgnoreCaseAndIsCancelledFalse(category);
	}

	public List<Event> searchEventsByLocation(String location) {
		return eventRepository.findByLocationIgnoreCaseAndIsCancelledFalse(location);
	}

	public List<Event> searchEventsByDateTime(LocalDateTime dateTime) {
		return eventRepository.findByDateTimeAndIsCancelledFalse(dateTime);
	}

    public Event save(Event event) {
        return eventRepository.save(event);
    }
}
