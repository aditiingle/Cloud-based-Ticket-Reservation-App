package com.ticket.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();
    private ApiService apiService;
    private TextView tvUserName, tvUserAvatar;
    private TextView tvFeaturedName, tvFeaturedLocation, tvFeaturedCategory;
    private TextView catAll, catConcert, catMovie, catSports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserAvatar = findViewById(R.id.tvUserAvatar);
        tvFeaturedName = findViewById(R.id.tvFeaturedName);
        tvFeaturedLocation = findViewById(R.id.tvFeaturedLocation);
        tvFeaturedCategory = findViewById(R.id.tvFeaturedCategory);

        catAll = findViewById(R.id.catAll);
        catConcert = findViewById(R.id.catConcert);
        catMovie = findViewById(R.id.catMovie);
        catSports = findViewById(R.id.catSports);

        catAll.setOnClickListener(v -> filterByCategory("All"));
        catConcert.setOnClickListener(v -> filterByCategory("Concert"));
        catMovie.setOnClickListener(v -> filterByCategory("Movie"));
        catSports.setOnClickListener(v -> filterByCategory("Sports"));

        // Buttons for navigation (using IDs from layout_bottom_nav included in activity_event_list)
        View btnSearch = findViewById(R.id.btnGoToSearch);
        if (btnSearch != null) btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        View btnTickets = findViewById(R.id.btnGoToMyTickets);
        if (btnTickets != null) btnTickets.setOnClickListener(v -> startActivity(new Intent(this, MyTicketsActivity.class)));

        View btnProfile = findViewById(R.id.btnGoToProfile);
        if (btnProfile != null) btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(filteredEvents);
        recyclerView.setAdapter(eventAdapter);

        apiService = RetrofitClient.getApiService();
        fetchEvents();
        fetchUserProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_tickets) {
            startActivity(new Intent(this, MyTicketsActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchUserProfile() {
        String token = SessionManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) return;

        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String name = response.body().getName();
                    if (name != null && !name.isEmpty()) {
                        tvUserName.setText(name + " \uD83D\uDC4B");
                        tvUserAvatar.setText(name.substring(0, 1).toUpperCase());
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("EventListActivity", "Failed to fetch profile: " + t.getMessage());
            }
        });
    }

    private void fetchEvents() {
        String token = SessionManager.getInstance(this).getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No session token found", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAllEvents("Bearer " + token).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEvents.clear();
                    allEvents.addAll(response.body());
                    updateFeaturedEvent();
                    filterByCategory("All");

                    if (allEvents.isEmpty()) {
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

    private void updateFeaturedEvent() {
        if (!allEvents.isEmpty()) {
            Event featured = allEvents.get(0);
            tvFeaturedName.setText(featured.getName());
            tvFeaturedLocation.setText(featured.getLocation());
            tvFeaturedCategory.setText(featured.getCategory() != null ? featured.getCategory().toUpperCase() : "EVENT");
        }
    }

    private void filterByCategory(String category) {
        filteredEvents.clear();
        if (category.equals("All")) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event e : allEvents) {
                if (e.getCategory() != null && e.getCategory().equalsIgnoreCase(category)) {
                    filteredEvents.add(e);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();

        // Update UI styles for categories
        updateCategoryStyles(category);
    }

    private void updateCategoryStyles(String selectedCategory) {
        int selectedBg = R.drawable.bg_category_selected;
        int unselectedBg = R.drawable.bg_category;
        int selectedText = getResources().getColor(R.color.white);
        int unselectedText = 0xFF161C2D;

        catAll.setBackgroundResource(selectedCategory.equals("All") ? selectedBg : unselectedBg);
        catAll.setTextColor(selectedCategory.equals("All") ? selectedText : unselectedText);

        catConcert.setBackgroundResource(selectedCategory.equals("Concert") ? selectedBg : unselectedBg);
        catConcert.setTextColor(selectedCategory.equals("Concert") ? selectedText : unselectedText);

        catMovie.setBackgroundResource(selectedCategory.equals("Movie") ? selectedBg : unselectedBg);
        catMovie.setTextColor(selectedCategory.equals("Movie") ? selectedText : unselectedText);

        catSports.setBackgroundResource(selectedCategory.equals("Sports") ? selectedBg : unselectedBg);
        catSports.setTextColor(selectedCategory.equals("Sports") ? selectedText : unselectedText);
    }
}
