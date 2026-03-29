package com.ticket.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.model.Ticket;
import com.ticket.reservation.repository.EventRepository;
import com.ticket.reservation.repository.ReservationRepository;
import com.ticket.reservation.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private NotificationService notificationService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, reservationRepository, ticketRepository, notificationService);
    }

    @Test
    void getAllEvents_returnsAllEvents() {
        Event event1 = new Event();
        event1.setName("Jazz Night");
        Event event2 = new Event();
        event2.setName("Rock Concert");

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));

        List<Event> result = eventService.getAllEvents();

        assertEquals(2, result.size());
        assertEquals("Jazz Night", result.get(0).getName());
    }

    @Test
    void getAllEvents_returnsEmptyList_whenNoEvents() {
        when(eventRepository.findAll()).thenReturn(List.of());

        List<Event> result = eventService.getAllEvents();

        assertEquals(0, result.size());
    }

    @Test
    void searchEvents_returnsMatchingEvents() {
        Event event = new Event();
        event.setName("Rock Concert");

        when(eventRepository.findByNameContainingIgnoreCaseAndIsCancelledFalse("rock"))
                .thenReturn(List.of(event));

        List<Event> result = eventService.searchEvents("rock");

        assertEquals(1, result.size());
        assertEquals("Rock Concert", result.get(0).getName());
    }

    @Test
    void searchEventsByFilter_returnsMatchingEvents() {
        Event event = new Event();
        event.setName("Rock Concert");
        event.setCategory("Concert");
        event.setLocation("Montreal");
        event.setDateTime(LocalDateTime.of(2026, 6, 15, 19, 0, 0));

        when(eventRepository.findByCategoryIgnoreCaseAndIsCancelledFalse("concert"))
                .thenReturn(List.of(event));
        when(eventRepository.findByLocationIgnoreCaseAndIsCancelledFalse("montreal"))
                .thenReturn(List.of(event));
        when(eventRepository.findByDateTimeBetweenAndIsCancelledFalse(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        List<Event> result1 = eventService.searchEventsByCategory("concert");
        List<Event> result2 = eventService.searchEventsByLocation("montreal");
        List<Event> result3 = eventService.searchEventsByDate(LocalDate.of(2026, 6, 15));

        assertEquals(1, result1.size());
        assertEquals("Rock Concert", result1.get(0).getName());

        assertEquals(1, result2.size());
        assertEquals("Rock Concert", result2.get(0).getName());

        assertEquals(1, result3.size());
        assertEquals("Rock Concert", result3.get(0).getName());
    }

    @Test
    void getEventById_returnsEvent_whenExists() {
        Event event = new Event();
        event.setId("event1");
        event.setName("Test Event");

        when(eventRepository.findById("event1")).thenReturn(Optional.of(event));

        Optional<Event> result = eventService.getEventById("event1");

        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getName());
    }

    @Test
    void getEventById_returnsEmpty_whenNotFound() {
        when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Event> result = eventService.getEventById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void addEvent_savesEventWithCancelledFalse() {
        Event event = new Event();
        event.setName("New Event");
        event.setCancelled(true); // Even if set to true, should be overridden

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId("newId");
            return savedEvent;
        });

        Event result = eventService.addEvent(event);

        assertNotNull(result.getId());
        assertFalse(result.isCancelled()); // Should be false
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void editEvent_updatesEventAndSendsNotifications() {
        Event existingEvent = new Event();
        existingEvent.setId("event1");
        existingEvent.setName("Old Name");
        existingEvent.setCategory("Concert");
        existingEvent.setDescription("Old description");
        existingEvent.setLocation("Old Location");
        existingEvent.setPrice(50.0);

        Event updatedEvent = new Event();
        updatedEvent.setName("New Name");
        updatedEvent.setCategory("Theater");
        updatedEvent.setDescription("New description");
        updatedEvent.setLocation("New Location");
        updatedEvent.setDateTime(LocalDateTime.now().plusDays(7));
        updatedEvent.setPrice(75.0);

        Ticket ticket1 = new Ticket("res1", "event1", 50.0);
        ticket1.setId("ticket1");
        Ticket ticket2 = new Ticket("res2", "event1", 50.0);
        ticket2.setId("ticket2");

        Reservation reservation1 = new Reservation();
        reservation1.setId("res1");
        reservation1.setCustomerId("customer1");

        Reservation reservation2 = new Reservation();
        reservation2.setId("res2");
        reservation2.setCustomerId("customer2");

        when(eventRepository.findById("event1")).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByEventId("event1")).thenReturn(List.of(ticket1, ticket2));
        when(reservationRepository.findById("res1")).thenReturn(Optional.of(reservation1));
        when(reservationRepository.findById("res2")).thenReturn(Optional.of(reservation2));

        Event result = eventService.editEvent("event1", updatedEvent);

        assertEquals("New Name", result.getName());
        assertEquals("Theater", result.getCategory());
        assertEquals("New description", result.getDescription());
        assertEquals("New Location", result.getLocation());
        assertEquals(75.0, result.getPrice());

        // Verify cascade logic: notifications sent for each reservation (2 reservations = 2 notifications)
        verify(notificationService, times(1)).sendEventUpdateNotification(eq("res1"), any(Event.class));
        verify(notificationService, times(1)).sendEventUpdateNotification(eq("res2"), any(Event.class));
        verify(ticketRepository, times(1)).findByEventId("event1");
    }

    @Test
    void editEvent_doesNotSendNotifications_whenNoReservations() {
        Event existingEvent = new Event();
        existingEvent.setId("event1");
        existingEvent.setName("Old Name");

        Event updatedEvent = new Event();
        updatedEvent.setName("New Name");

        when(eventRepository.findById("event1")).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByEventId("event1")).thenReturn(List.of());

        Event result = eventService.editEvent("event1", updatedEvent);

        assertEquals("New Name", result.getName());
        verify(notificationService, never()).sendEventUpdateNotification(anyString(), any(Event.class));
    }

    @Test
    void editEvent_throwsException_whenEventNotFound() {
        Event updatedEvent = new Event();
        updatedEvent.setName("New Name");

        when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.editEvent("nonexistent", updatedEvent);
        });
    }

    @Test
    void cancelEvent_setsCancelledAndCancelsReservations() {
        Event event = new Event();
        event.setId("event1");
        event.setName("Test Event");
        event.setCancelled(false);

        Ticket ticket1 = new Ticket("res1", "event1", 50.0);
        ticket1.setId("ticket1");
        Ticket ticket2 = new Ticket("res2", "event1", 50.0);
        ticket2.setId("ticket2");

        Reservation reservation1 = new Reservation();
        reservation1.setId("res1");
        reservation1.setCustomerId("customer1");
        reservation1.setStatus("CONFIRMED");

        Reservation reservation2 = new Reservation();
        reservation2.setId("res2");
        reservation2.setCustomerId("customer2");
        reservation2.setStatus("CONFIRMED");

        when(eventRepository.findById("event1")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByEventId("event1")).thenReturn(List.of(ticket1, ticket2));
        when(reservationRepository.findById("res1")).thenReturn(Optional.of(reservation1));
        when(reservationRepository.findById("res2")).thenReturn(Optional.of(reservation2));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.cancelEvent("event1");

        assertTrue(result.isCancelled());

        // Verify cascade logic: reservations cancelled
        verify(reservationRepository, times(2)).save(any(Reservation.class));

        // Verify cascade logic: event cancelled notifications sent (using reservation ID and event)
        verify(notificationService, times(1)).sendEventCancelledNotification(eq("res1"), any(Event.class));
        verify(notificationService, times(1)).sendEventCancelledNotification(eq("res2"), any(Event.class));
    }

    @Test
    void cancelEvent_handlesNoReservations() {
        Event event = new Event();
        event.setId("event1");
        event.setName("Test Event");
        event.setCancelled(false);

        when(eventRepository.findById("event1")).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.findByEventId("event1")).thenReturn(List.of());

        Event result = eventService.cancelEvent("event1");

        assertTrue(result.isCancelled());
        verify(notificationService, never()).sendEventCancelledNotification(anyString(), any(Event.class));
    }

    @Test
    void cancelEvent_throwsException_whenEventNotFound() {
        when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            eventService.cancelEvent("nonexistent");
        });
    }
}
