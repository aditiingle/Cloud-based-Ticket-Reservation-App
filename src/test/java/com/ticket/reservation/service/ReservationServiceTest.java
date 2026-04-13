package com.ticket.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Notification;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.model.Ticket;
import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.EventRepository;
import com.ticket.reservation.repository.ReservationRepository;
import com.ticket.reservation.repository.TicketRepository;
import com.ticket.reservation.repository.UserRepository;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private TicketRepository ticketRepository;
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private ReservationService service;

    @BeforeEach
    void setUp() {
        reservationRepository = Mockito.mock(ReservationRepository.class);
        ticketRepository = Mockito.mock(TicketRepository.class);
        eventRepository = Mockito.mock(EventRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        notificationService = Mockito.mock(NotificationService.class);

        service = new ReservationService(
                reservationRepository,
                ticketRepository,
                eventRepository,
                userRepository,
                notificationService
        );
    }

    @Test
    void createReservation_returnsSavedReservation() {

        // Arrange
        User user = new User("Test User", "testuser@email.com", "5555552222", "password");
        user.setId("customer1");

        Event event = new Event();
        event.setId("event1");
        event.setName("Rock Concert");
        event.setLocation("Montreal");
        event.setDateTime(LocalDateTime.of(2026, 4, 1, 19, 0));
        event.setPrice(45.0);
        event.setCancelled(false);

        Reservation savedReservation = new Reservation("customer1", LocalDateTime.now(), "ACTIVE");
        savedReservation.setId("reservation1");

        Notification notification = new Notification(
                "reservation1",
                "Booking confirmed",
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        Mockito.when(userRepository.findById("customer1")).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById("event1")).thenReturn(Optional.of(event));
        Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenReturn(savedReservation);

        // Act
        Reservation result = service.createReservation("customer1", "event1");

        // Assert
        assertNotNull(result);
        assertEquals("reservation1", result.getId());
        assertEquals("customer1", result.getCustomerId());
        assertEquals("ACTIVE", result.getStatus());

        Mockito.verify(ticketRepository).save(Mockito.any(Ticket.class));
        Mockito.verify(notificationService).sendBookingConfirmationAsync("reservation1", event, user);
    }

    @Test
    void createReservation_throwsException_whenUserNotFound() {

        // Arrange
        Mockito.when(userRepository.findById("customer1")).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createReservation("customer1", "event1")
        );

        // Assert
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void createReservation_throwsException_whenEventNotFound() {

        // Arrange
        User user = new User("Test User", "testuser@email.com", "5555552222", "password");
        user.setId("customer1");

        Mockito.when(userRepository.findById("customer1")).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById("event1")).thenReturn(Optional.empty());

        //Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createReservation("customer1", "event1")
        );

        // Assert
        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void createReservation_throwsException_whenEventIsCancelled() {

        // Arrange
        User user = new User("Test User", "testuser@email.com", "5555552222", "password");
        user.setId("customer1");

        Event event = new Event();
        event.setId("event1");
        event.setCancelled(true);

        Mockito.when(userRepository.findById("customer1")).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById("event1")).thenReturn(Optional.of(event));

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createReservation("customer1", "event1")
        );

        // Assert
        assertEquals("Event has been cancelled", exception.getMessage());
    }

    @Test
    void cancelReservation_returnsCancelledReservation() {

        // Arrange
        Reservation reservation = new Reservation("customer1", LocalDateTime.now(), "ACTIVE");
        reservation.setId("reservation1");

        Ticket ticket = new Ticket("reservation1", "event1", 45.0);

        Event event = new Event();
        event.setId("event1");
        event.setName("Rock Concert");
        event.setLocation("Montreal");
        event.setDateTime(LocalDateTime.of(2026, 4, 1, 19, 0));

        User user = new User("Test User", "testuser@email.com", "5555552222", "password");
        user.setId("customer1");

        Notification notification = new Notification(
                "reservation1",
                "Reservation cancelled",
                LocalDateTime.now(),
                "CANCELLATION"
        );

        Mockito.when(reservationRepository.findById("reservation1")).thenReturn(Optional.of(reservation));
        Mockito.when(reservationRepository.save(Mockito.any(Reservation.class))).thenReturn(reservation);
        Mockito.when(ticketRepository.findByReservationId("reservation1")).thenReturn(Optional.of(ticket));
        Mockito.when(eventRepository.findById("event1")).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findById("customer1")).thenReturn(Optional.of(user));

        // Act
        Reservation result = service.cancelReservation("customer1", "reservation1");

        //Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());

        Mockito.verify(notificationService).sendCancellationNotificationAsync("reservation1", event, user);
    }

    @Test
    void cancelReservation_throwsException_whenReservationNotFound() {

        // Arrange
        Mockito.when(reservationRepository.findById("reservation1")).thenReturn(Optional.empty());

        //Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelReservation("customer1", "reservation1")
        );

        // Assert
        assertEquals("Reservation not found", exception.getMessage());
    }

    @Test
    void cancelReservation_throwsException_whenUnauthorized() {

        // Arrange
        Reservation reservation = new Reservation("customer2", LocalDateTime.now(), "ACTIVE");
        reservation.setId("reservation1");

        Mockito.when(reservationRepository.findById("reservation1")).thenReturn(Optional.of(reservation));

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelReservation("customer1", "reservation1")
        );

        // Assert
        assertEquals("Unauthorized", exception.getMessage());
    }
}