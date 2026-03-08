package com.ticket.reservation.service;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> searchEvents(String name) {
        return eventRepository.findByNameContainingIgnoreCaseAndIsCancelledFalse(name);
    }

	public List<Event> searchEventsByCategory(String category) {
		return eventRepository.findByCategoryIgnoreCase(category);
	}

	public List<Event> searchEventsByLocation(String location) {
		return eventRepository.findByLocationIgnoreCase(location);
	}

	public List<Event> searchEventsByDate(LocalDateTime date) {
		return eventRepository.findByDate(date);
	}

    public Event save(Event event) {
        return eventRepository.save(event);
    }
}
