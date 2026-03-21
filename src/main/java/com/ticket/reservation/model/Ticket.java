package com.ticket.reservation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;

    private String reservationId;

    private String eventId;

    private double price;

    public Ticket() {}

    public Ticket(String reservationId, String eventId, double price) {
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
