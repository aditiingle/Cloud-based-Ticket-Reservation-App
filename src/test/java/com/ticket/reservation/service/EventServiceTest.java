package com.ticket.reservation.service;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

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

        Mockito.when(repo.findByCategoryIgnoreCase("concert"))
                .thenReturn(List.of(event));
		Mockito.when(repo.findByLocationIgnoreCase("montreal"))
                .thenReturn(List.of(event));
		Mockito.when(repo.findByDate(LocalDateTime.of(1, 1, 1, 1, 1, 1)))
                .thenReturn(List.of(event));


        List<Event> result1 = service.searchEventsByCategory("concert");
        List<Event> result2 = service.searchEventsByLocation("montreal");
        List<Event> result3 = service.searchEventsByDate(LocalDateTime.of(1, 1, 1, 1, 1, 1));

        assertEquals(1, result1.size());
        assertEquals("Rock Concert", result1.get(0).getName());

        assertEquals(1, result2.size());
        assertEquals("Rock Concert", result2.get(0).getName());

		assertEquals(1, result3.size());
        assertEquals("Rock Concert", result3.get(0).getName());
	}
}
