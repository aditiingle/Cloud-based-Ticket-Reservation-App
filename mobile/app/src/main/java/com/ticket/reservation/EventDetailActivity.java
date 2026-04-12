package com.ticket.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ticket.reservation.model.CreateReservationRequest;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    private TextView tvEventName, tvEventDescription, tvEventLocation, tvEventDateTime, tvEventPrice, tvTopPrice, tvCategoryTag;
    private Button btnBookTicket, btnEditEvent, btnCancelEvent;
    private ImageView btnBack;
    private ApiService apiService;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        tvEventName = findViewById(R.id.tvEventName);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventDateTime = findViewById(R.id.tvEventDateTime);
        tvEventPrice = findViewById(R.id.tvEventPrice);
        tvTopPrice = findViewById(R.id.tvTopPrice);
        tvCategoryTag = findViewById(R.id.tvCategoryTag);
        btnBookTicket = findViewById(R.id.btnBookTicket);
        btnBack = findViewById(R.id.btnBack);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnCancelEvent = findViewById(R.id.btnCancelEvent);

        apiService = RetrofitClient.getApiService();
        eventId = getIntent().getStringExtra("EVENT_ID");

        if (eventId != null) {
            fetchEventDetails();
            checkExistingReservation();
        }

        btnBookTicket.setOnClickListener(v -> bookTicket());

        if (btnEditEvent != null) {
            btnEditEvent.setOnClickListener(v -> {
                if (currentEvent != null) {
                    Intent intent = new Intent(this, AddEditEventActivity.class);
                    intent.putExtra("EVENT_ID", currentEvent.getId());
                    startActivity(intent);
                }
            });
        }

        if (btnCancelEvent != null) {
            btnCancelEvent.setOnClickListener(v -> cancelEvent());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void checkExistingReservation() {
        String token = SessionManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) return;

        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String customerId = response.body().getId();
                    String email = response.body().getEmail();

                    // Admin check
                    if (email != null && email.contains("admin")) {
                        if (btnEditEvent != null) btnEditEvent.setVisibility(View.VISIBLE);
                        if (btnCancelEvent != null) btnCancelEvent.setVisibility(View.VISIBLE);
                        btnBookTicket.setVisibility(View.GONE);
                    } else {
                        if (btnEditEvent != null) btnEditEvent.setVisibility(View.GONE);
                        if (btnCancelEvent != null) btnCancelEvent.setVisibility(View.GONE);
                        btnBookTicket.setVisibility(View.VISIBLE);
                    }

                    apiService.getUserReservations("Bearer " + token, customerId).enqueue(new Callback<List<Reservation>>() {
                        @Override
                        public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> responseRes) {
                            if (responseRes.isSuccessful() && responseRes.body() != null) {
                                for (Reservation res : responseRes.body()) {
                                    if (eventId.equals(res.getEventId()) && !"CANCELLED".equals(res.getStatus())) {
                                        btnBookTicket.setEnabled(false);
                                        btnBookTicket.setText("Already Reserved");
                                        break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Reservation>> call, Throwable t) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void fetchEventDetails() {
        String token = SessionManager.getInstance(this).getToken();
        apiService.getEventById("Bearer " + token, eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentEvent = response.body();
                    tvEventName.setText(currentEvent.getName());
                    tvEventDescription.setText(currentEvent.getDescription());
                    tvEventLocation.setText(currentEvent.getLocation());
                    tvEventDateTime.setText(currentEvent.getDateTime() != null ? currentEvent.getDateTime().toString() : "");
                    tvEventPrice.setText("$" + currentEvent.getPrice());
                    if (tvTopPrice != null) tvTopPrice.setText("$" + currentEvent.getPrice());
                    if (tvCategoryTag != null) tvCategoryTag.setText(currentEvent.getCategory() != null ? currentEvent.getCategory().toUpperCase() : "EVENT");

                    if (currentEvent.isCancelled()) {
                        btnBookTicket.setEnabled(false);
                        btnBookTicket.setText("Event Cancelled");
                        if (btnCancelEvent != null) btnCancelEvent.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(EventDetailActivity.this, "Failed to load event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bookTicket() {
        String token = SessionManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // We first need the current user's ID to make a reservation
        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String customerId = response.body().getId();
                    sendReservationRequest(token, customerId);
                } else {
                    Toast.makeText(EventDetailActivity.this, "Failed to get user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendReservationRequest(String token, String customerId) {
        CreateReservationRequest request = new CreateReservationRequest(customerId, eventId);
        apiService.reserveTicket("Bearer " + token, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailActivity.this, "Ticket reserved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back after booking
                } else {
                    Toast.makeText(EventDetailActivity.this, "Failed to reserve ticket: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelEvent() {
        String token = SessionManager.getInstance(this).getToken();
        apiService.cancelEvent("Bearer " + token, eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailActivity.this, "Event cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EventDetailActivity.this, "Failed to cancel event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
