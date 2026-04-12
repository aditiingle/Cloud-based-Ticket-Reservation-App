package com.ticket.reservation.service;

import java.time.LocalDateTime;
import com.ticket.reservation.model.*;
import org.springframework.stereotype.Service;

import com.ticket.reservation.repository.EventRepository;
import com.ticket.reservation.repository.ReservationRepository;
import com.ticket.reservation.repository.TicketRepository;
import com.ticket.reservation.repository.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ReservationService(
            ReservationRepository reservationRepository,
            TicketRepository ticketRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Reservation createReservation(String customerId, String eventId) {

        User user = userRepository.findById(customerId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            throw new IllegalArgumentException("Event not found");
        }
        if (event.isCancelled()) {
            throw new IllegalArgumentException("Event has been cancelled");
        }

        Reservation reservation = new Reservation(customerId, LocalDateTime.now(), "ACTIVE");
        Reservation savedReservation = reservationRepository.save(reservation);

        Ticket ticket = new Ticket(
                savedReservation.getId(),
                eventId,
                event.getPrice()
        );
        ticketRepository.save(ticket);

        notificationService.sendBookingConfirmationAsync(savedReservation.getId(), event, user);

        return savedReservation;
    }

    public Reservation cancelReservation(String customerId, String reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }
        if (!reservation.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        Ticket ticket = ticketRepository.findByReservationId(reservationId).orElse(null);
        if (ticket != null) {
            Event event = eventRepository.findById(ticket.getEventId()).orElse(null);
            if (event != null) {
                User user = userRepository.findById(customerId).orElse(null);
                notificationService.sendCancellationNotificationAsync(reservationId, event, user);
            }
        }

        return reservation;
    }
}
