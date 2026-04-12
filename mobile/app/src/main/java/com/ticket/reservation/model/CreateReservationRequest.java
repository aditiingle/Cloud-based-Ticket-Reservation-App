package com.ticket.reservation.model;

public class CreateReservationRequest {
    private String customerId;
    private String eventId;

    public CreateReservationRequest(String customerId, String eventId) {
        this.customerId = customerId;
        this.eventId = eventId;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
}
