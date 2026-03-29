package com.ticket.reservation;

import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.LoginRequest;
import com.ticket.reservation.model.LoginResponse;
import com.ticket.reservation.model.RegisterRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/users")
    Call<Void> register(@Body RegisterRequest request);

    @GET("api/events")
    Call<List<Event>> getAllEvents(@retrofit2.http.Header("Authorization") String authHeader);
}