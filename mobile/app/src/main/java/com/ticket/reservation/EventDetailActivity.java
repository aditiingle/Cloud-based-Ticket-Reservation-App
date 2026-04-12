package com.ticket.reservation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ticket.reservation.model.CreateReservationRequest;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    private TextView tvEventName, tvEventDescription, tvEventLocation, tvEventDateTime, tvEventPrice, tvTopPrice, tvCategoryTag;
    private Button btnBookTicket;
    private ImageView btnBack;
    private ApiService apiService;
    private String eventId;

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

        apiService = RetrofitClient.getApiService();
        eventId = getIntent().getStringExtra("EVENT_ID");

        if (eventId != null) {
            fetchEventDetails();
        }

        btnBookTicket.setOnClickListener(v -> bookTicket());

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void fetchEventDetails() {
        String token = SessionManager.getInstance(this).getToken();
        apiService.getEventById("Bearer " + token, eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Event event = response.body();
                    tvEventName.setText(event.getName());
                    tvEventDescription.setText(event.getDescription());
                    tvEventLocation.setText(event.getLocation());
                    tvEventDateTime.setText(event.getDateTime() != null ? event.getDateTime().toString() : "");
                    tvEventPrice.setText("$" + event.getPrice());
                    if (tvTopPrice != null) tvTopPrice.setText("$" + event.getPrice());
                    if (tvCategoryTag != null) tvCategoryTag.setText(event.getCategory() != null ? event.getCategory().toUpperCase() : "EVENT");
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
}
