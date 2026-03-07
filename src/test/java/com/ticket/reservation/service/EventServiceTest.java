package com.ticket.reservation.service;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
}