package com.ticket.reservation.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/search")
    public List<Event> searchEvents(@RequestParam String name) {
        return eventService.searchEvents(name);
    }

	@GetMapping("/search/category")
	public List<Event> searchEventsByCategory(@RequestParam String category) {
		return eventService.searchEventsByCategory(category);
	}

	@GetMapping("/search/location")
	public List<Event> searchEventsByLocation(@RequestParam String location) {
		return eventService.searchEventsByLocation(location);
	}

	@GetMapping("/search/datetime")
	public List<Event> searchEventsByDateTime(@RequestParam int year,
			@RequestParam int month, @RequestParam int dayOfMonth,
			@RequestParam int hour, @RequestParam int minute) {
		return eventService.searchEventsByDateTime(LocalDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0));
	}

}
