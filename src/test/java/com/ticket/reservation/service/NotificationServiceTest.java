package com.ticket.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Notification;
import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.NotificationRepository;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void save_setsSentAtWhenMissing() {
        Notification notification = new Notification();
        notification.setReservationId("res123");
        notification.setMessage("Test notification");
        notification.setType("BOOKING_CONFIRMATION");

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = notificationService.save(notification);

        assertNotNull(saved);
        assertNotNull(saved.getSentAt());
        assertEquals("res123", saved.getReservationId());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void getNotificationsByReservationId_returnsMatchingNotifications() {
        Notification notification = new Notification(
                "res123",
                "Booking confirmed",
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        when(notificationRepository.findByReservationId("res123"))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getNotificationsByReservationId("res123");

        assertEquals(1, result.size());
        assertEquals("res123", result.get(0).getReservationId());
        assertEquals("BOOKING_CONFIRMATION", result.get(0).getType());
    }

    @Test
    void getNotificationsByType_returnsMatchingNotifications() {
        Notification notification = new Notification(
                "res123",
                "Reservation cancelled",
                LocalDateTime.now(),
                "CANCELLATION"
        );

        when(notificationRepository.findByType("CANCELLATION"))
                .thenReturn(List.of(notification));

        List<Notification> result = notificationService.getNotificationsByType("CANCELLATION");

        assertEquals(1, result.size());
        assertEquals("CANCELLATION", result.get(0).getType());
    }

    @Test
    void sendBookingConfirmation_createsAndSavesNotification() {
        Event event = new Event();
        event.setName("Rock Concert");
        event.setLocation("Montreal");
        event.setDateTime(LocalDateTime.of(2026, 4, 10, 19, 30));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.sendBookingConfirmation("res123", event);

        assertNotNull(result);
        assertEquals("res123", result.getReservationId());
        assertEquals("BOOKING_CONFIRMATION", result.getType());
        assertTrue(result.getMessage().contains("Reservation ID: res123"));
        assertTrue(result.getMessage().contains("Rock Concert"));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void sendCancellationNotification_createsAndSavesNotification() {
        Event event = new Event();
        event.setName("Jazz Night");
        event.setLocation("Toronto");
        event.setDateTime(LocalDateTime.of(2026, 5, 1, 20, 0));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.sendCancellationNotification("res456", event);

        assertNotNull(result);
        assertEquals("CANCELLATION", result.getType());
        assertTrue(result.getMessage().contains("Reservation cancelled"));
        assertTrue(result.getMessage().contains("Jazz Night"));
    }

    @Test
    void sendEventUpdateNotification_createsAndSavesNotification() {
        Event event = new Event();
        event.setName("Movie Premiere");
        event.setLocation("Vancouver");
        event.setDateTime(LocalDateTime.of(2026, 6, 15, 18, 0));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.sendEventUpdateNotification("res789", event);

        assertNotNull(result);
        assertEquals("EVENT_UPDATE", result.getType());
        assertTrue(result.getMessage().contains("Event updated"));
        assertTrue(result.getMessage().contains("Movie Premiere"));
    }

    @Test
    void sendEventCancelledNotification_createsAndSavesNotification() {
        Event event = new Event();
        event.setName("Soccer Final");
        event.setLocation("Ottawa");
        event.setDateTime(LocalDateTime.of(2026, 7, 20, 21, 0));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.sendEventCancelledNotification("res999", event);

        assertNotNull(result);
        assertEquals("EVENT_CANCELLED", result.getType());
        assertTrue(result.getMessage().contains("Event cancelled"));
        assertTrue(result.getMessage().contains("Soccer Final"));
    }

    @Test
    void sendEmail_returnsTrueWhenUserHasEmail() {
        User user = new User("Samz", "samz@email.com", "1234567890", "hashedPassword");
        Notification notification = new Notification(
                "res123",
                "Booking confirmed",
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        boolean result = notificationService.sendEmail(user, notification);

        assertTrue(result);
    }

    @Test
    void sendEmail_returnsFalseWhenUserHasNoEmail() {
        User user = new User("Samz", null, "1234567890", "hashedPassword");
        Notification notification = new Notification(
                "res123",
                "Booking confirmed",
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        boolean result = notificationService.sendEmail(user, notification);

        assertFalse(result);
    }

    @Test
    void sendSMS_returnsTrueWhenUserHasPhone() {
        User user = new User("Samz", "samz@email.com", "1234567890", "hashedPassword");
        Notification notification = new Notification(
                "res123",
                "Booking confirmed",
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        boolean result = notificationService.sendSMS(user, notification);

        assertTrue(result);
    }

    @Test
    void sendSMS_returnsFalseWhenUserHasNoPhone() {
        User user = new User("Samz", "samz@email.com", null, "hashedPassword");
        Notification notification = new Notification(
                "res123",
                "Booking confirmed",
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        boolean result = notificationService.sendSMS(user, notification);

        assertFalse(result);
    }
}