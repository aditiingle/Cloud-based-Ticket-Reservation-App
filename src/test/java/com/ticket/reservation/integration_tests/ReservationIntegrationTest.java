package com.ticket.reservation.integration_tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.ticket.reservation.controller.ReservationController;
import com.ticket.reservation.dto.CancelReservationRequestDTO;
import com.ticket.reservation.dto.CreateReservationRequestDTO;
import com.ticket.reservation.model.Reservation;
import com.ticket.reservation.repository.ReservationRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class ReservationIntegrationTest {

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Test
    public void testCreateReservation() {
        CreateReservationRequestDTO reservationDTO = new CreateReservationRequestDTO();
        reservationDTO.setCustomerId("TEMP");
        reservationDTO.setEventId("TEMP");

        ResponseEntity<?> response = reservationController.createReservation(reservationDTO);
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertTrue(reservationRepository.findByCustomerId("TEMP").size() > 0);

        Optional<Reservation> reservationOptional = reservationRepository.findByCustomerId("TEMP").stream().findFirst();
        assertTrue(reservationOptional.isPresent());

        Reservation reservation = reservationOptional.get();
        reservationRepository.delete(reservation);
        assertTrue(reservationRepository.findById(reservation.getId()).isEmpty());
    }

    @Test
    public void testCancelReservation() {
        CreateReservationRequestDTO reservationDTO = new CreateReservationRequestDTO();
        reservationDTO.setCustomerId("TEMP");
        reservationDTO.setEventId("TEMP");

        CancelReservationRequestDTO cancelDTO = new CancelReservationRequestDTO();
        cancelDTO.setCustomerId("TEMP");
        cancelDTO.setReservationId("TEMP");

        ResponseEntity<?> createResponse = reservationController.createReservation(reservationDTO);
        assertTrue(createResponse.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertTrue(reservationRepository.findByCustomerId("TEMP").size() > 0);

        ResponseEntity<?> cancelResponse = reservationController.cancelReservation(cancelDTO);
        assertTrue(cancelResponse.getStatusCode().isSameCodeAs(HttpStatus.OK));

        Optional<Reservation> reservationOptional = reservationRepository.findById(cancelDTO.getReservationId());
        assertTrue(reservationOptional.isPresent());

        Reservation reservation = reservationOptional.get();
        assertTrue(reservation.getStatus().equals("CANCELLED"));
        reservation.setStatus("ACTIVE");
        reservationRepository.save(reservation);
        assertTrue(reservationRepository.findById(reservation.getId()).isPresent());
    }
}
