package com.ticket.reservation;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.Event;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private final List<Event> events = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(events);
        recyclerView.setAdapter(eventAdapter);

        apiService = RetrofitClient.getApiService();
        fetchEvents();
    }

    private void fetchEvents() {
        String token = SessionManager.getInstance().getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No session token found", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAllEvents("Bearer " + token).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    events.clear();
                    events.addAll(response.body());
                    eventAdapter.notifyDataSetChanged();

                    if (events.isEmpty()) {
                        Toast.makeText(EventListActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EventListActivity.this,
                            "Failed to load events: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(EventListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}