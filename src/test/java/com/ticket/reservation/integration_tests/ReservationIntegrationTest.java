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

import java.util.List;
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
        String userID = "69c98ecb31810e05e842a720";
        String eventID = "69c9939414fdac0e8958544b";
        reservationDTO.setCustomerId(userID);
        reservationDTO.setEventId(eventID);

        ResponseEntity<?> response = reservationController.createReservation(reservationDTO);
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertTrue(reservationRepository.findByCustomerId(userID).size() > 0);

        Optional<Reservation> reservationOptional = reservationRepository.findByCustomerId(userID).stream().findFirst();
        assertTrue(reservationOptional.isPresent());

        Reservation reservation = reservationOptional.get();
        reservationRepository.delete(reservation);
        assertTrue(reservationRepository.findById(reservation.getId()).isEmpty());
    }

    @Test
    public void testCancelReservation() {
        String userID = "69c98ecb31810e05e842a720";
        List<Reservation> reservations = reservationRepository.findByCustomerId(userID);
        assertTrue(reservations.size() > 0);

        String reservationID = reservations.get(0).getId();

        CancelReservationRequestDTO cancelDTO = new CancelReservationRequestDTO();
        cancelDTO.setCustomerId(userID);
        cancelDTO.setReservationId(reservationID);

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
