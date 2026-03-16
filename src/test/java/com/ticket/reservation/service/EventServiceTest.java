package com.ticket.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;

class EventServiceTest {

    @Test
    void getAllEvents_returnsAllEvents() {
        EventRepository repo = Mockito.mock(EventRepository.class);
        EventService service = new EventService(repo);

        Event event1 = new Event();
        event1.setName("Jazz Night");
        Event event2 = new Event();
        event2.setName("Rock Concert");

        Mockito.when(repo.findAll()).thenReturn(List.of(event1, event2));

        List<Event> result = service.getAllEvents();

        assertEquals(2, result.size());
        assertEquals("Jazz Night", result.get(0).getName());
    }

    @Test
    void getAllEvents_returnsEmptyList_whenNoEvents() {
        EventRepository repo = Mockito.mock(EventRepository.class);
        EventService service = new EventService(repo);

        Mockito.when(repo.findAll()).thenReturn(List.of());

        List<Event> result = service.getAllEvents();

        assertEquals(0, result.size());
    }

    @Test
    void searchEvents_returnsMatchingEvents() {

        EventRepository repo = Mockito.mock(EventRepository.class);
        EventService service = new EventService(repo);

        Event event = new Event();
        event.setName("Rock Concert");

        Mockito.when(repo.findByNameContainingIgnoreCaseAndIsCancelledFalse("rock"))
                .thenReturn(List.of(event));

        List<Event> result = service.searchEvents("rock");

        assertEquals(1, result.size());
        assertEquals("Rock Concert", result.get(0).getName());
    }

	@Test
	void searchEventsByFilter_returnsMatchingEvents() {
		EventRepository repo = Mockito.mock(EventRepository.class);
        EventService service = new EventService(repo);

        Event event = new Event();
        event.setName("Rock Concert");
		event.setCategory("Concert");
		event.setLocation("Montreal");
		event.setDateTime(LocalDateTime.of(1, 1, 1, 1, 1, 1));

        Mockito.when(repo.findByCategoryIgnoreCaseAndIsCancelledFalse("concert"))
                .thenReturn(List.of(event));
		Mockito.when(repo.findByLocationIgnoreCaseAndIsCancelledFalse("montreal"))
                .thenReturn(List.of(event));
		Mockito.when(repo.findByDateTimeAndIsCancelledFalse(LocalDateTime.of(1, 1, 1, 1, 1, 1)))
                .thenReturn(List.of(event));


        List<Event> result1 = service.searchEventsByCategory("concert");
        List<Event> result2 = service.searchEventsByLocation("montreal");
        List<Event> result3 = service.searchEventsByDateTime(LocalDateTime.of(1, 1, 1, 1, 1, 1));

        assertEquals(1, result1.size());
        assertEquals("Rock Concert", result1.get(0).getName());

        assertEquals(1, result2.size());
        assertEquals("Rock Concert", result2.get(0).getName());

		assertEquals(1, result3.size());
        assertEquals("Rock Concert", result3.get(0).getName());
	}
}
