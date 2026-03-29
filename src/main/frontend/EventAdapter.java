package com.ticket.reservation.frontend;

import com.ticket.reservation.model.Event;

import java.util.List;

public class EventAdapter {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public Event getItem(int position) {
        return events.get(position);
    }

    // Binds event data to a row view:
    // tvEventName     ← event.getName()
    // tvEventCategory ← event.getCategory()
    // tvEventLocation ← event.getLocation()
    // tvEventDateTime ← event.getDateTime()
    // tvEventPrice    ← "$" + event.getPrice()
}
