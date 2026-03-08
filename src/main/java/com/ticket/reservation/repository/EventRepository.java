package com.ticket.reservation.repository;

import com.ticket.reservation.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByNameContainingIgnoreCaseAndIsCancelledFalse(String name);

}