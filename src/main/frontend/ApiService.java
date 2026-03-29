package com.ticket.reservation.frontend;

import com.ticket.reservation.model.Event;

import java.util.List;

public interface ApiService {

    // POST /api/auth/login
    // Body: LoginRequest { email, password }
    // Returns: LoginResponse { token }
    Object login(LoginRequest request);

    // POST /api/auth/register
    // Body: RegisterRequest { email, phone, password }
    // Returns: 200 OK on success
    void register(RegisterRequest request);

    // GET /api/events
    // Returns: list of all available events
    List<Event> getAllEvents();
}
