package com.ticket.reservation;

import com.ticket.reservation.model.CancelReservationRequest;
import com.ticket.reservation.model.Event;
import com.ticket.reservation.model.LoginRequest;
import com.ticket.reservation.model.LoginResponse;
import com.ticket.reservation.model.CreateReservationRequest;
import com.ticket.reservation.model.RegisterRequest;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/users")
    Call<Void> register(@Body RegisterRequest request);

    @GET("api/events")
    Call<List<Event>> getAllEvents(@Header("Authorization") String authHeader);

    @GET("api/events/{id}")
    Call<Event> getEventById(@Header("Authorization") String authHeader, @Path("id") String id);

    @GET("api/events/search")
    Call<List<Event>> searchEvents(@Header("Authorization") String authHeader, @Query("name") String name);

    @GET("api/events/search/location")
    Call<List<Event>> searchEventsByLocation(@Header("Authorization") String authHeader, @Query("location") String location);

    @GET("api/events/search/category")
    Call<List<Event>> searchEventsByCategory(@Header("Authorization") String authHeader, @Query("category") String category);

    @GET("api/events/search/date")
    Call<List<Event>> searchEventsByDate(@Header("Authorization") String authHeader, @Query("year") int year, @Query("month") int month, @Query("day") int day);

    @POST("api/reservations/reserve")
    Call<Object> reserveTicket(@Header("Authorization") String authHeader, @Body CreateReservationRequest request);

    @POST("api/reservations/cancel")
    Call<Reservation> cancelReservation(@Header("Authorization") String authHeader, @Body CancelReservationRequest request);

    @GET("api/users/me")
    Call<User> getUserProfile(@Header("Authorization") String authHeader);

    @GET("api/reservations/user/{customerId}")
    Call<List<Reservation>> getUserReservations(@Header("Authorization") String authHeader, @Path("customerId") String customerId);
}
