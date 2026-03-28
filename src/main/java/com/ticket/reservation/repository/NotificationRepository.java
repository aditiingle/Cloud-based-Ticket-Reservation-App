package com.ticket.reservation.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ticket.reservation.model.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByReservationId(String reservationId);

    List<Notification> findByType(String type);
}