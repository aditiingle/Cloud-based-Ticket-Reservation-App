package com.ticket.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ticket.reservation.model.Event;

public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByNameContainingIgnoreCaseAndIsCancelledFalse(String name);
	List<Event> findByCategoryIgnoreCaseAndIsCancelledFalse(String category);
	List<Event> findByLocationIgnoreCaseAndIsCancelledFalse(String location);
	List<Event> findByDateTimeAndIsCancelledFalse(LocalDateTime dateTime);
}
