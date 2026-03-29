package com.ticket.reservation.integration_tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.ticket.reservation.controller.UserController;
import com.ticket.reservation.dto.LoginRequestDTO;
import com.ticket.reservation.dto.UserRegistrationRequestDTO;
import com.ticket.reservation.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class UserIntegrationTest {
	
    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        UserRegistrationRequestDTO requestDTO = new UserRegistrationRequestDTO();

        requestDTO.setName("testuser");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("test@email.com");

        ResponseEntity<?> response = userController.createUser(requestDTO);
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.CREATED));
        assertTrue(userRepository.findByEmail("test@email.com").isPresent());
        assertTrue(userRepository.deleteByEmail("test@email.com").isPresent());
    }

    @Test
    public void testLogin() {
        LoginRequestDTO loginDTO = new LoginRequestDTO();

        loginDTO.setIdentifier("login@test.com");
        loginDTO.setPassword("password123");

        ResponseEntity<?> response = userController.login(loginDTO);

        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetCurrentUser() {
        ResponseEntity<?> response = userController.getCurrentUser("login@test.com");
        
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
    }
}
