package com.ticket.reservation.model;

public class Reservation {
    private String id;
    private String customerId;
    private String bookingDate;
    private String status;
    private String eventId; // Added for frontend convenience if needed

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }
    public String getEventId() { return eventId; }
}
