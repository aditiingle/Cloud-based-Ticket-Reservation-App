package com.ticket.reservation.integration_tests;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.ticket.reservation.controller.EventController;
import com.ticket.reservation.dto.EventRequestDTO;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class EventIntegrationTest {
    
    @Autowired
    private EventController eventController;

    @Autowired
    private EventRepository eventRepository;

    @Test
    private void testAddEvent() {
        EventRequestDTO eventDTO = new EventRequestDTO();
        eventDTO.setName("Test event");
        eventDTO.setCategory("Test category");
        eventDTO.setDescription("Test description");
        eventDTO.setLocation("Test location");
        eventDTO.setDateTime(LocalDateTime.now().plusDays(1));
        eventDTO.setPrice(10.0);

        ResponseEntity<?> response = eventController.addEvent(eventDTO);
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.CREATED));
        assertTrue(eventRepository.findByName("Test event").isPresent());
        assertTrue(eventRepository.deleteByName("Test event").isPresent());
    }

    @Test
    private void testEditEvent() {
        String id = "";

        EventRequestDTO eventDTO = new EventRequestDTO();
        eventDTO.setName("Edit test event");
        eventDTO.setCategory("Test category");
        eventDTO.setDescription("Test description");
        eventDTO.setLocation("Test location");
        eventDTO.setDateTime(LocalDateTime.now().plusDays(1));
        eventDTO.setPrice(10.0);

        ResponseEntity<?> response = eventController.editEvent(id, eventDTO);
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertTrue(eventRepository.findByName("Edit test event").isPresent());
    }

    @Test 
    public void testCancelEvent() {
        Optional<Event> eventOptional = eventRepository.findByName("Delete test event");
        assertTrue(eventOptional.isPresent());

        String id = eventOptional.get().getId();
        ResponseEntity<?> cancelResponse = eventController.cancelEvent(id);
        assertTrue(cancelResponse.getStatusCode().isSameCodeAs(HttpStatus.OK));

        eventOptional = eventRepository.findById(id);
        assertTrue(eventOptional.isPresent());

        Event event = eventOptional.get();
        assertTrue(event.isCancelled());
        event.setCancelled(false);
        assertNotNull(eventRepository.save(event));
    }

    @Test
    public void testGetAllEvents() {
        assertTrue(eventController.getAllEvents().size() >= 0);
    }

    @Test
    public void testGetEventById() {
        List<Event> events = eventController.getAllEvents();
        assertTrue(events.size() >= 0);

        String id = events.get(0).getId();
        ResponseEntity<Event> response = eventController.getEventById(id);
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertEquals(id, response.getBody().getId());
    }

    @Test
    public void testSearchEvents() {
        assertTrue(eventController.searchEvents("test").size() >= 0);
        assertTrue(eventController.searchEventsByCategory("Test category").size() >= 0);
        assertTrue(eventController.searchEventsByLocation("Test location").size() >= 0);
        assertTrue(eventController.searchEventsByDate(2026, 3, 30).size() >= 0);
    }
}