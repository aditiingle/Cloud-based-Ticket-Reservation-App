package com.ticket.reservation.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String reservationId;

    private String message;

    private LocalDateTime sentAt;

    private String type;

    public Notification() {}

    public Notification(String reservationId, String message, LocalDateTime sentAt, String type) {
        this.reservationId = reservationId;
        this.message = message;
        this.sentAt = sentAt;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
