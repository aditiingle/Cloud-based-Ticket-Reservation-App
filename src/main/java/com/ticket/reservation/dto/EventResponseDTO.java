package com.ticket.reservation.dto;

import java.time.LocalDateTime;

public class EventResponseDTO {

    private String id;
    private String name;
    private String category;
    private String description;
    private String location;
    private LocalDateTime dateTime;
    private double price;
    private boolean cancelled;

    public EventResponseDTO() {}

    public EventResponseDTO(String id, String name, String category, String description, 
                           String location, LocalDateTime dateTime, double price, boolean cancelled) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.price = price;
        this.cancelled = cancelled;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
