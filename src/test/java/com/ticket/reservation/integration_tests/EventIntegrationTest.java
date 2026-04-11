package com.ticket.reservation.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper; // Converts Java objects (DTOs) into JSON strings for HTTP request bodies
import com.ticket.reservation.dto.EventRequestDTO;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.repository.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Enables MockMvc so we can simulate real HTTP requests without starting browser
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Simulates an authenticated user with a given role for secured endpoints
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc; // For performing HTTP requests in Spring tests

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

// Static imports for HTTP request builders
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=false", // Disable Swagger
        "springdoc.swagger-ui.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    // Helper method to build a valid EventRequestDTO
    private EventRequestDTO buildEventRequest(String name) {
        EventRequestDTO dto = new EventRequestDTO();
        dto.setName(name);
        dto.setCategory("Test category");
        dto.setDescription("Test description");
        dto.setLocation("Test location");
        dto.setDateTime(LocalDateTime.now().plusDays(1));
        dto.setPrice(10.0);
        return dto;
    }

    // Helper method to save an Event entity into the database
    private Event saveEvent(String name) {
        Event event = new Event();
        event.setName(name);
        event.setCategory("Test category");
        event.setDescription("Test description");
        event.setLocation("Test location");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setPrice(10.0);
        event.setCancelled(false);
        return eventRepository.save(event);
    }

    // Test POST /api/events endpoint
    @Test
    @WithMockUser(roles = "ADMIN") // Simulate an authenticated admin
    public void testAddEvent() throws Exception {
        EventRequestDTO eventDTO =  buildEventRequest("Test event");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Test event"))
                .andExpect(jsonPath("$.category").value("Test category"))
                .andExpect(jsonPath("$.location").value("Test location"))
                .andExpect(jsonPath("$.cancelled").value(false));

        Event savedEvent = eventRepository.findByName("Test event").orElse(null);
        assertNotNull(savedEvent);
        assertEquals("Test category", savedEvent.getCategory());
    }

    // Test PUT /api/events/{id} endpoint
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditEvent() throws Exception {
        Event existingEvent = saveEvent("Edit test event");
        EventRequestDTO updatedDTO = buildEventRequest("Updated event");
        updatedDTO.setCategory("Updated category");
        updatedDTO.setLocation("Updated location");
        updatedDTO.setPrice(25.0);

        mockMvc.perform(put("/api/events/{id}", existingEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingEvent.getId()))
                .andExpect(jsonPath("$.name").value("Updated event"))
                .andExpect(jsonPath("$.category").value("Updated category"))
                .andExpect(jsonPath("$.location").value("Updated location"))
                .andExpect(jsonPath("$.price").value(25.0));

        Event updatedEvent = eventRepository.findById(existingEvent.getId()).orElse(null);
        assertNotNull(updatedEvent);
        assertEquals("Updated event", updatedEvent.getName());
        assertEquals("Updated category", updatedEvent.getCategory());
        assertEquals("Updated location", updatedEvent.getLocation());
        assertEquals(25.0, updatedEvent.getPrice());
    }

    // Test PATCH /api/events/{id}/cancel endpoint
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCancelEvent() throws Exception {
        Event existingEvent = saveEvent("Cancel test event");

        mockMvc.perform(patch("/api/events/{id}/cancel", existingEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingEvent.getId()))
                .andExpect(jsonPath("$.cancelled").value(true));

        Event cancelledEvent = eventRepository.findById(existingEvent.getId()).orElse(null);
        assertNotNull(cancelledEvent);
        assertTrue(cancelledEvent.isCancelled());
    }

    // Test DELETE /api/events/{id} endpoint
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteEventCancelsEvent() throws Exception {
        Event existingEvent = saveEvent("Delete test event");

        mockMvc.perform(delete("/api/events/{id}", existingEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingEvent.getId()))
                .andExpect(jsonPath("$.cancelled").value(true));

        Event cancelledEvent = eventRepository.findById(existingEvent.getId()).orElse(null);
        assertNotNull(cancelledEvent);
        assertTrue(cancelledEvent.isCancelled());
    }

    // Test GET /api/events endpoint
    @Test
    @WithMockUser
    public void testGetAllEvents() throws Exception {
        saveEvent("Event 1");
        saveEvent("Event 2");

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // Test GET /api/events/{id} endpoint when the event exists
    @Test
    @WithMockUser
    public void testGetEventById() throws Exception {
        Event event = saveEvent("Single event");
        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.getId()))
                .andExpect(jsonPath("$.name").value("Single event"));
    }

    // Test GET /api/events/{id} endpoint when the event doesn't exist
    @Test
    @WithMockUser
    public void testGetEventByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/events/{id}", "nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    // Test GET /api/events/search?name=... endpoint
    @Test
    @WithMockUser
    public void testSearchEventsByName() throws Exception {
        saveEvent("Concert Night");

        mockMvc.perform(get("/api/events/search")
                        .param("name", "Concert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Concert Night"));
    }

    // Test GET /api/events/search/category?category=... endpoint
    @Test
    @WithMockUser
    public void testSearchEventsByCategory() throws Exception {
        saveEvent("Category event");

        mockMvc.perform(get("/api/events/search/category")
                        .param("category", "Test category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("Test category"));
    }

    // Test GET /api/events/search/location?location=... endpoint
    @Test
    @WithMockUser
    public void testSearchEventsByLocation() throws Exception {
        saveEvent("Location event");

        mockMvc.perform(get("/api/events/search/location")
                        .param("location", "Test location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].location").value("Test location"));
    }

    // Tests the GET /api/events/search/date endpoint
    @Test
    @WithMockUser
    public void testSearchEventsByDate() throws Exception {
        Event event = saveEvent("Date event");
        LocalDateTime dateTime = event.getDateTime();

        mockMvc.perform(get("/api/events/search/date")
                        .param("year", String.valueOf(dateTime.getYear()))
                        .param("month", String.valueOf(dateTime.getMonthValue()))
                        .param("day", String.valueOf(dateTime.getDayOfMonth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Date event"));
    }
}