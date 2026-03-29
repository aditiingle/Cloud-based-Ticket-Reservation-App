package com.ticket.reservation.model;

public class Event {
    private String id;
    private String name;
    private String category;
    private String description;
    private String location;
    private String dateTime;
    private double price;
    private boolean isCancelled;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDateTime() { return dateTime; }
    public double getPrice() { return price; }
    public boolean isCancelled() { return isCancelled; }
}