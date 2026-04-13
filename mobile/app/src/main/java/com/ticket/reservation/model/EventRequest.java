package com.ticket.reservation.model;

public class EventRequest {
    private String name;
    private String category;
    private String description;
    private String location;
    private String dateTime;
    private double price;

    public EventRequest(String name, String category, String description, String location, String dateTime, double price) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.price = price;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
