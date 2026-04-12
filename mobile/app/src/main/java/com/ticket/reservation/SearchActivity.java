package com.ticket.reservation;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.Event;

import java.util.Calendar;
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

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

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

        catSearchConcerts.setOnClickListener(v -> searchByCategories(new String[]{"Concert", "Concerts"}));
        catSearchSports.setOnClickListener(v -> searchByCategory("Sports"));
        catSearchMovies.setOnClickListener(v -> searchByCategories(new String[]{"Movie", "Movies"}));
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
        
        // Reset results and perform multi-parameter search
        searchResults.clear();
        
        // 1. Search by Name
        apiService.searchEvents("Bearer " + token, query).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mergeResults(response.body());
                }
                
                // 2. Search by Location (always try both)
                apiService.searchEventsByLocation("Bearer " + token, query).enqueue(new Callback<List<Event>>() {
                    @Override
                    public void onResponse(Call<List<Event>> call, Response<List<Event>> responseLoc) {
                        if (responseLoc.isSuccessful() && responseLoc.body() != null) {
                            mergeResults(responseLoc.body());
                        }
                        eventAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<List<Event>> call, Throwable t) {
                        eventAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Search error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mergeResults(List<Event> newEvents) {
        for (Event e : newEvents) {
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

    public void showDatePickerDialog(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Month is 0-indexed in DatePicker
                    searchByDate(year1, monthOfYear + 1, dayOfMonth);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void searchByDate(int year, int month, int day) {
        String token = SessionManager.getInstance(this).getToken();
        apiService.searchEventsByDate("Bearer " + token, year, month, day).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    eventAdapter.notifyDataSetChanged();
                    if (searchResults.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "No events on this date", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void searchByCategories(String[] categories) {
        String token = SessionManager.getInstance(this).getToken();
        searchResults.clear();
        final int[] remaining = {categories.length};

        for (String category : categories) {
            apiService.searchEventsByCategory("Bearer " + token, category).enqueue(new Callback<List<Event>>() {
                @Override
                public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (Event e : response.body()) {
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
                    checkFinished();
                }

                @Override
                public void onFailure(Call<List<Event>> call, Throwable t) {
                    checkFinished();
                }

                private void checkFinished() {
                    remaining[0]--;
                    if (remaining[0] <= 0) {
                        eventAdapter.notifyDataSetChanged();
                        if (searchResults.isEmpty()) {
                            Toast.makeText(SearchActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
