package com.ticket.reservation.controller;

import com.ticket.reservation.service.EventService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    @Test
    void getAllEvents_returns200() throws Exception {
        EventService service = Mockito.mock(EventService.class);
        EventController controller = new EventController(service);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    @Test
    void searchEvents_returns200() throws Exception {

        EventService service = Mockito.mock(EventService.class);
        EventController controller = new EventController(service);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/events/search?name=rock"))
                .andExpect(status().isOk());
    }
}