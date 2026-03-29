package com.ticket.reservation.frontend;

import com.ticket.reservation.model.Event;

import java.util.List;

public class EventListActivity {

    private List<Event> events;

    public EventListActivity(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEvents() {
        return events;
    }

    public int getEventCount() {
        return events != null ? events.size() : 0;
    }

    public Event getEvent(int index) {
        if (events == null || index < 0 || index >= events.size()) {
            return null;
        }
        return events.get(index);
    }
}
