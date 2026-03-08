package com.ticket.reservation.controller;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/search")
    public List<Event> searchEvents(@RequestParam String name) {
        return eventService.searchEvents(name);
    }

	@GetMapping("/search/category/{category}")
	public List<Event> searchEventsByCatergory(@PathVariable String category){
		return eventService.searchEventsByCategory(category);
	}

	@GetMapping("/search/location/{location}")
	public List<Event> searchEventsByLocation(@PathVariable String location) {
		return eventService.searchEventsByLocation(location);
	}

	@GetMapping("/search/date/{year}/{month}/{dayOfMonth}/{hour}/{minute}")
	public List<Event> searchEventsByDate(@PathVariable int year,
			@PathVariable int month, @PathVariable int dayOfMonth,
			@PathVariable int hour, @PathVariable int minute) {
		return eventService.searchEventsByDate(LocalDateTime.of(year, month, dayOfMonth, hour, minute, 0, 0));
	}

}
