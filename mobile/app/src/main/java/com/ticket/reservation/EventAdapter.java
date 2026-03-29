package com.ticket.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.tvEventName.setText(event.getName());
        holder.tvEventCategory.setText(event.getCategory());
        holder.tvEventLocation.setText(event.getLocation());
        holder.tvEventDateTime.setText(event.getDateTime());
        holder.tvEventPrice.setText("$" + event.getPrice());
    }

    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvEventCategory;
        TextView tvEventLocation;
        TextView tvEventDateTime;
        TextView tvEventPrice;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
        }
    }
}