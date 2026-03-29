package com.ticket.reservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class EventRequestDTO {

    @NotBlank(message = "Event name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    private LocalDateTime dateTime;

    @Positive(message = "Price must be positive")
    private double price;

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
}
