package com.ticket.reservation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.reservation.dto.EventRequestDTO;
import com.ticket.reservation.dto.EventResponseDTO;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.service.EventService;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void getAllEvents_returns200() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    @Test
    void searchEvents_returns200() throws Exception {
        when(eventService.searchEvents("rock")).thenReturn(List.of());

        mockMvc.perform(get("/api/events/search?name=rock"))
                .andExpect(status().isOk());
    }

    @Test
    void searchEventsByFilter_returns200() throws Exception {
        when(eventService.searchEventsByCategory("concert")).thenReturn(List.of());
        when(eventService.searchEventsByLocation("montreal")).thenReturn(List.of());
        when(eventService.searchEventsByDate(any(LocalDate.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/events/search/category?category=concert"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/search/location?location=montreal"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/events/search/date?year=2026&month=6&day=15"))
                .andExpect(status().isOk());
    }

    @Test
    void getEventById_returns200_whenEventExists() throws Exception {
        Event event = new Event();
        event.setId("event1");
        event.setName("Test Event");

        when(eventService.getEventById("event1")).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/event1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"));
    }

    @Test
    void getEventById_returns404_whenEventNotFound() throws Exception {
        when(eventService.getEventById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEvent_returns201() throws Exception {
        EventRequestDTO eventRequest = new EventRequestDTO();
        eventRequest.setName("New Event");
        eventRequest.setCategory("Concert");
        eventRequest.setPrice(50.0);

        EventResponseDTO responseDTO = new EventResponseDTO(
            "newEventId", "New Event", "Concert", null, null, null, 50.0, false
        );

        when(eventService.addEventFromDTO(any(EventRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Event"))
                .andExpect(jsonPath("$.id").value("newEventId"));
    }

    @Test
    void editEvent_returns200_whenEventExists() throws Exception {
        EventRequestDTO updateRequest = new EventRequestDTO();
        updateRequest.setName("New Name");
        updateRequest.setCategory("Theater");
        updateRequest.setDescription("Updated description");
        updateRequest.setLocation("New Location");
        updateRequest.setDateTime(LocalDateTime.now().plusDays(7));
        updateRequest.setPrice(75.0);

        EventResponseDTO responseDTO = new EventResponseDTO(
            "event1", "New Name", "Theater", "Updated description", "New Location", 
            updateRequest.getDateTime(), 75.0, false
        );

        when(eventService.editEventFromDTO(anyString(), any(EventRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/events/event1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.category").value("Theater"));
    }

    @Test
    void editEvent_returns404_whenEventNotFound() throws Exception {
        EventRequestDTO updateRequest = new EventRequestDTO();
        updateRequest.setName("New Name");

        when(eventService.editEventFromDTO(anyString(), any(EventRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Event not found"));

        mockMvc.perform(put("/api/events/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelEvent_returns200_whenEventExists() throws Exception {
        EventResponseDTO responseDTO = new EventResponseDTO(
            "event1", "Cancelled Event", null, null, null, null, 0.0, true
        );

        when(eventService.cancelEventAndReturnDTO("event1")).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/events/event1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true));
    }

    @Test
    void cancelEvent_returns404_whenEventNotFound() throws Exception {
        when(eventService.cancelEventAndReturnDTO("nonexistent"))
                .thenThrow(new IllegalArgumentException("Event not found"));

        mockMvc.perform(patch("/api/events/nonexistent/cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_returns200_whenEventExists() throws Exception {
        EventResponseDTO responseDTO = new EventResponseDTO(
            "event1", "Deleted Event", null, null, null, null, 0.0, true
        );

        when(eventService.cancelEventAndReturnDTO("event1")).thenReturn(responseDTO);

        mockMvc.perform(delete("/api/events/event1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelled").value(true));
    }

    @Test
    void deleteEvent_returns404_whenEventNotFound() throws Exception {
        when(eventService.cancelEventAndReturnDTO("nonexistent"))
                .thenThrow(new IllegalArgumentException("Event not found"));

        mockMvc.perform(delete("/api/events/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
