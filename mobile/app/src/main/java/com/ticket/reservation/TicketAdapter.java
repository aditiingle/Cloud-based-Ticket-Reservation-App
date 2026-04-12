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

    public interface OnCancelClickListener {
        void onCancelClick(Reservation reservation);
    }

    private final List<Reservation> tickets;
    private final OnCancelClickListener cancelClickListener;

    public TicketAdapter(List<Reservation> tickets, OnCancelClickListener cancelClickListener) {
        this.tickets = tickets;
        this.cancelClickListener = cancelClickListener;
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
        holder.tvEventName.setText("Reserved on: " + (ticket.getBookingDate() != null ? ticket.getBookingDate().split("T")[0] : "N/A"));

        if ("CANCELLED".equals(ticket.getStatus())) {
            holder.btnCancelTicket.setVisibility(View.GONE);
        } else {
            holder.btnCancelTicket.setVisibility(View.VISIBLE);
            holder.btnCancelTicket.setOnClickListener(v -> {
                if (cancelClickListener != null) {
                    cancelClickListener.onCancelClick(ticket);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tickets == null ? 0 : tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId, tvTicketQuantityPrice, tvEventName;
        View btnCancelTicket;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvTicketQuantityPrice = itemView.findViewById(R.id.tvTicketQuantityPrice);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            btnCancelTicket = itemView.findViewById(R.id.btnCancelTicket);
        }
    }
}
