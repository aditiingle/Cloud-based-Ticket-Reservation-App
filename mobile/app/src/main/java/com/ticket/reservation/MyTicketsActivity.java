package com.ticket.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.CancelReservationRequest;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTickets;
    private TicketAdapter ticketAdapter;
    private final List<Reservation> tickets = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        recyclerViewTickets = findViewById(R.id.recyclerViewTickets);
        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));

        ticketAdapter = new TicketAdapter(tickets, this::showCancelConfirmation);
        recyclerViewTickets.setAdapter(ticketAdapter);

        apiService = RetrofitClient.getApiService();
        fetchMyTickets();

        // Bottom Nav
        View btnHome = findViewById(R.id.btnGoToHome);
        if (btnHome != null) btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        View btnSearch = findViewById(R.id.btnGoToSearch);
        if (btnSearch != null) btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        View btnProfile = findViewById(R.id.btnGoToProfile);
        if (btnProfile != null) btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void fetchMyTickets() {
        String token = SessionManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) return;

        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String customerId = response.body().getId();
                    loadReservations(token, customerId);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MyTicketsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadReservations(String token, String customerId) {
        apiService.getUserReservations("Bearer " + token, customerId).enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tickets.clear();
                    tickets.addAll(response.body());
                    ticketAdapter.notifyDataSetChanged();

                    if (tickets.isEmpty()) {
                        Toast.makeText(MyTicketsActivity.this, "No tickets found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyTicketsActivity.this, "Failed to load tickets: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Toast.makeText(MyTicketsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCancelConfirmation(Reservation reservation) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Ticket")
                .setMessage("Are you sure you want to cancel this reservation?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelTicket(reservation))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelTicket(Reservation reservation) {
        String token = SessionManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) return;

        CancelReservationRequest request = new CancelReservationRequest(reservation.getCustomerId(), reservation.getId());

        apiService.cancelReservation("Bearer " + token, request).enqueue(new Callback<Reservation>() {
            @Override
            public void onResponse(Call<Reservation> call, Response<Reservation> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyTicketsActivity.this, "Ticket cancelled successfully", Toast.LENGTH_SHORT).show();
                    fetchMyTickets(); // Refresh list
                } else {
                    Toast.makeText(MyTicketsActivity.this, "Failed to cancel: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Reservation> call, Throwable t) {
                Toast.makeText(MyTicketsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
