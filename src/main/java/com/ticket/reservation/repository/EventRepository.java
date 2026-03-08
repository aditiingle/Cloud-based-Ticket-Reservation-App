package com.ticket.reservation.repository;

import com.ticket.reservation.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByNameContainingIgnoreCaseAndIsCancelledFalse(String name);
	List<Event> findByCategoryIgnoreCase(String category);
	List<Event> findByLocationIgnoreCase(String location);
	List<Event> findByDate(LocalDateTime date);
}
