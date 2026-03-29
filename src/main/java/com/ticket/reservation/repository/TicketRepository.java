package com.ticket.reservation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ticket.reservation.model.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    Optional<Ticket> findByReservationId(String reservationId);
    List<Ticket> findByEventId(String eventId);
}
