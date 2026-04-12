package com.ticket.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerViewSearch;
    private EventAdapter eventAdapter;
    private final List<Event> searchResults = new ArrayList<>();
    private ApiService apiService;
    private TextView catSearchConcerts, catSearchSports, catSearchMovies, catSearchTravel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearch);
        recyclerViewSearch = findViewById(R.id.recyclerViewSearch);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(searchResults);
        recyclerViewSearch.setAdapter(eventAdapter);

        apiService = RetrofitClient.getApiService();

        catSearchConcerts = findViewById(R.id.catSearchConcerts);
        catSearchSports = findViewById(R.id.catSearchSports);
        catSearchMovies = findViewById(R.id.catSearchMovies);
        catSearchTravel = findViewById(R.id.catSearchTravel);

        catSearchConcerts.setOnClickListener(v -> searchByCategory("Concert"));
        catSearchSports.setOnClickListener(v -> searchByCategory("Sports"));
        catSearchMovies.setOnClickListener(v -> searchByCategory("Movie"));
        catSearchTravel.setOnClickListener(v -> searchByCategory("Travel"));

        // Bottom Nav
        View btnHome = findViewById(R.id.btnGoToHome);
        if (btnHome != null) btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        View btnTickets = findViewById(R.id.btnGoToMyTickets);
        if (btnTickets != null) btnTickets.setOnClickListener(v -> startActivity(new Intent(this, MyTicketsActivity.class)));

        View btnProfile = findViewById(R.id.btnGoToProfile);
        if (btnProfile != null) btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    performSearch(s.toString());
                } else {
                    searchResults.clear();
                    eventAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        String token = SessionManager.getInstance(this).getToken();
        
        // Use a combined approach: search by name, then by location if name yield few results
        // or just trigger both and merge (for simplicity here, we'll try name first)
        apiService.searchEvents("Bearer " + token, query).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    
                    // Also try searching by location and add those results
                    apiService.searchEventsByLocation("Bearer " + token, query).enqueue(new Callback<List<Event>>() {
                        @Override
                        public void onResponse(Call<List<Event>> call, Response<List<Event>> responseLoc) {
                            if (responseLoc.isSuccessful() && responseLoc.body() != null) {
                                for (Event e : responseLoc.body()) {
                                    boolean exists = false;
                                    for (Event existing : searchResults) {
                                        if (existing.getId().equals(e.getId())) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) searchResults.add(e);
                                }
                            }
                            eventAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<List<Event>> call, Throwable t) {
                            eventAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Search error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchByCategory(String category) {
        String token = SessionManager.getInstance(this).getToken();
        apiService.searchEventsByCategory("Bearer " + token, category).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    eventAdapter.notifyDataSetChanged();
                    
                    if (searchResults.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "No events found for " + category, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
