package com.ticket.reservation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.EventRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText etEventName, etEventCategory, etEventLocation, etEventPrice, etEventDate, etEventTime, etEventDescription;
    private Button btnSaveEvent;
    private ImageView btnBack;
    private TextView tvTitle;
    private ApiService apiService;
    private String eventId; // If null, we're adding; if not, we're editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        tvTitle = findViewById(R.id.tvTitle);
        etEventName = findViewById(R.id.etEventName);
        etEventCategory = findViewById(R.id.etEventCategory);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventPrice = findViewById(R.id.etEventPrice);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventDescription = findViewById(R.id.etEventDescription);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnBack = findViewById(R.id.btnBack);

        apiService = RetrofitClient.getApiService();
        eventId = getIntent().getStringExtra("EVENT_ID");

        if (eventId != null) {
            tvTitle.setText("Edit Event");
            fetchEventDetails();
        } else {
            tvTitle.setText("Add New Event");
        }

        btnSaveEvent.setOnClickListener(v -> saveEvent());
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
                    etEventName.setText(event.getName());
                    etEventCategory.setText(event.getCategory());
                    etEventLocation.setText(event.getLocation());
                    etEventPrice.setText(String.valueOf(event.getPrice()));
                    if (event.getDateTime() != null && event.getDateTime().contains("T")) {
                        String[] parts = event.getDateTime().split("T");
                        etEventDate.setText(parts[0]);
                        etEventTime.setText(parts[1].substring(0, 5)); // HH:MM
                    }
                    etEventDescription.setText(event.getDescription());
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Toast.makeText(AddEditEventActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEvent() {
        String name = etEventName.getText().toString().trim();
        String category = etEventCategory.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String priceStr = etEventPrice.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String time = etEventTime.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || location.isEmpty() || priceStr.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // ISO format: YYYY-MM-DDTHH:MM:SS
        String isoDateTime = date + "T" + time + ":00";

        EventRequest request = new EventRequest(name, category, description, location, isoDateTime, price);
        String token = SessionManager.getInstance(this).getToken();

        Call<Void> call;
        if (eventId != null) {
            call = apiService.editEvent("Bearer " + token, eventId, request);
        } else {
            call = apiService.addEvent("Bearer " + token, request);
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditEventActivity.this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditEventActivity.this, "Failed to save: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddEditEventActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
