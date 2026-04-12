package com.ticket.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ticket.reservation.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvAvatar;
    private Button btnLogout;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvAvatar = findViewById(R.id.tvAvatar);
        btnLogout = findViewById(R.id.btnLogout);

        apiService = RetrofitClient.getApiService();
        fetchUserProfile();

        btnLogout.setOnClickListener(v -> logout());

        // Bottom Nav
        View btnHome = findViewById(R.id.btnGoToHome);
        if (btnHome != null) btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        View btnSearch = findViewById(R.id.btnGoToSearch);
        if (btnSearch != null) btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        View btnTickets = findViewById(R.id.btnGoToMyTickets);
        if (btnTickets != null) btnTickets.setOnClickListener(v -> startActivity(new Intent(this, MyTicketsActivity.class)));
    }

    private void fetchUserProfile() {
        String token = SessionManager.getInstance(this).getToken();
        apiService.getUserProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvUserName.setText(user.getName());
                    tvUserEmail.setText(user.getEmail());
                    if (user.getName() != null && !user.getName().isEmpty()) {
                        tvAvatar.setText(user.getName().substring(0, 1).toUpperCase());
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        SessionManager.getInstance(this).clearSession();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
