package com.ticket.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.Reservation;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<Reservation> tickets;

    public TicketAdapter(List<Reservation> tickets) {
        this.tickets = tickets;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Reservation ticket = tickets.get(position);
        holder.tvTicketId.setText("#" + (ticket.getId() != null ? ticket.getId().substring(0, Math.min(ticket.getId().length(), 8)) : ""));
        holder.tvTicketQuantityPrice.setText("Status: " + ticket.getStatus());
        holder.tvEventName.setText("Reserved on: " + ticket.getBookingDate());
    }

    @Override
    public int getItemCount() {
        return tickets == null ? 0 : tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId, tvTicketQuantityPrice, tvEventName;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvTicketQuantityPrice = itemView.findViewById(R.id.tvTicketQuantityPrice);
            tvEventName = itemView.findViewById(R.id.tvEventName);
        }
    }
}
