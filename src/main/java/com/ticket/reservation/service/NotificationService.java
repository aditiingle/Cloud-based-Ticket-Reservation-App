package com.ticket.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Notification;
import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification save(Notification notification) {
        if (notification.getSentAt() == null) {
            notification.setSentAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByReservationId(String reservationId) {
        return notificationRepository.findByReservationId(reservationId);
    }

    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    public Notification sendBookingConfirmation(String reservationId, Event event) {
        String message = buildEventMessage(
                "Booking confirmed",
                reservationId,
                event
        );

        Notification notification = new Notification(
                reservationId,
                message,
                LocalDateTime.now(),
                "BOOKING_CONFIRMATION"
        );

        return notificationRepository.save(notification);
    }

    public Notification sendCancellationNotification(String reservationId, Event event) {
        String message = buildEventMessage(
                "Reservation cancelled",
                reservationId,
                event
        );

        Notification notification = new Notification(
                reservationId,
                message,
                LocalDateTime.now(),
                "CANCELLATION"
        );

        return notificationRepository.save(notification);
    }

    public Notification sendEventUpdateNotification(String reservationId, Event event) {
        String message = buildEventMessage(
                "Event updated",
                reservationId,
                event
        );

        Notification notification = new Notification(
                reservationId,
                message,
                LocalDateTime.now(),
                "EVENT_UPDATE"
        );

        return notificationRepository.save(notification);
    }

    public Notification sendEventCancelledNotification(String reservationId, Event event) {
        String message = buildEventMessage(
                "Event cancelled",
                reservationId,
                event
        );

        Notification notification = new Notification(
                reservationId,
                message,
                LocalDateTime.now(),
                "EVENT_CANCELLED"
        );

        return notificationRepository.save(notification);
    }

    public boolean sendEmail(User user, Notification notification) {
        return user != null
                && user.getEmail() != null
                && !user.getEmail().isBlank()
                && notification != null;
    }

    public boolean sendSMS(User user, Notification notification) {
        return user != null
                && user.getPhone() != null
                && !user.getPhone().isBlank()
                && notification != null;
    }

    private String buildEventMessage(String prefix, String reservationId, Event event) {
        String eventName = event != null && event.getName() != null ? event.getName() : "Unknown event";
        String location = event != null && event.getLocation() != null ? event.getLocation() : "TBD";
        String dateTime = event != null && event.getDateTime() != null ? event.getDateTime().toString() : "TBD";

        return prefix
                + ". Reservation ID: " + reservationId
                + ", Event: " + eventName
                + ", Location: " + location
                + ", Date/Time: " + dateTime;
    }
}