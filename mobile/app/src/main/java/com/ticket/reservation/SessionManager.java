package com.ticket.reservation;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "TicketAppSession";
    private static final String KEY_TOKEN = "jwt_token";
    private static SessionManager instance;
    private final SharedPreferences sharedPreferences;

    private SessionManager(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // Existing static getter for places where Context might be tricky but instance is guaranteed to exist
    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearSession() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
}
