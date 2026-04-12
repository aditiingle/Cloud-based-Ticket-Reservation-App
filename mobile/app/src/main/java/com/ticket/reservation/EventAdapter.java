package com.ticket.reservation;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.Event;

import java.util.List;

import com.squareup.picasso.Picasso;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> events;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public EventAdapter(List<Event> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
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

        String category = event.getCategory() != null ? event.getCategory().toLowerCase() : "event";
        String imageUrl = "https://loremflickr.com/800/400/" + category + "?lock=" + Math.abs(event.getId().hashCode());

        Picasso.get()
                .load(imageUrl)
                .fit()
                .centerCrop()
                .into(holder.ivEventImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            } else {
                Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                v.getContext().startActivity(intent);
            }
        });
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
        ImageView ivEventImage;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
        }
    }
}