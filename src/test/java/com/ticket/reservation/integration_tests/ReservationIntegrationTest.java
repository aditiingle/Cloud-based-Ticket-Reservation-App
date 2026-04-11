package com.ticket.reservation.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.reservation.dto.CancelReservationRequestDTO;
import com.ticket.reservation.dto.CreateReservationRequestDTO;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.EventRepository;
import com.ticket.reservation.repository.ReservationRepository;
import com.ticket.reservation.repository.TicketRepository;
import com.ticket.reservation.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User saveTestUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("reservation@test.com");
        user.setPassword("password123");
        return userRepository.save(user);
    }

    private Event saveTestEvent() {
        Event event = new Event();
        event.setName("Test Event");
        event.setCategory("Concert");
        event.setDescription("Integration test event");
        event.setLocation("Montreal");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setPrice(25.0);
        event.setCancelled(false);
        return eventRepository.save(event);
    }

    @Test
    @WithMockUser
    void testCreateReservation() throws Exception {
        User user = saveTestUser();
        Event event = saveTestEvent();

        CreateReservationRequestDTO requestDTO = new CreateReservationRequestDTO();
        requestDTO.setCustomerId(user.getId());
        requestDTO.setEventId(event.getId());

        mockMvc.perform(post("/api/reservations/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(user.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Reservation savedReservation = reservationRepository.findByCustomerId(user.getId()).get(0);
        assertEquals("ACTIVE", savedReservation.getStatus());
    }

    @Test
    @WithMockUser
    void testCancelReservation() throws Exception {
        User user = saveTestUser();
        Event event = saveTestEvent();

        CreateReservationRequestDTO createDTO = new CreateReservationRequestDTO();
        createDTO.setCustomerId(user.getId());
        createDTO.setEventId(event.getId());

        String createResponse = mockMvc.perform(post("/api/reservations/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Reservation createdReservation = objectMapper.readValue(createResponse, Reservation.class);

        CancelReservationRequestDTO cancelDTO = new CancelReservationRequestDTO();
        cancelDTO.setCustomerId(user.getId());
        cancelDTO.setReservationId(createdReservation.getId());

        mockMvc.perform(post("/api/reservations/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdReservation.getId()))
                .andExpect(jsonPath("$.customerId").value(user.getId()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Reservation cancelledReservation =
                reservationRepository.findById(createdReservation.getId()).orElse(null);

        assertNotNull(cancelledReservation);
        assertEquals("CANCELLED", cancelledReservation.getStatus());
    }
}