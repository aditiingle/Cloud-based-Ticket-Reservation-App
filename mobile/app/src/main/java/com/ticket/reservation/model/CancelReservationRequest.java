package com.ticket.reservation.model;

public class CancelReservationRequest {
    private String reservationId;
    private String customerId;

    public CancelReservationRequest(String customerId, String reservationId) {
        this.customerId = customerId;
        this.reservationId = reservationId;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
