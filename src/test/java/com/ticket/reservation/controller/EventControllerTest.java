package com.ticket.reservation.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ticket.reservation.service.EventService;

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

	@Test
	void searchEventsByFilter_returns200() throws Exception {
		EventService service = Mockito.mock(EventService.class);
        EventController controller = new EventController(service);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/events/search/category?category=concert"))
                .andExpect(status().isOk());

		mockMvc.perform(get("/api/events/search/location?location=montreal"))
                .andExpect(status().isOk());

		mockMvc.perform(get("/api/events/search/datetime?year=1&month=1&dayOfMonth=1&hour=1&minute=1"))
                .andExpect(status().isOk());
	}
}
