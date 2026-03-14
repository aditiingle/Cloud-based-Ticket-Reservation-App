package com.ticket.reservation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ticket.reservation.dto.LoginRequestDTO;
import com.ticket.reservation.dto.LoginResponseDTO;
import com.ticket.reservation.exception.AuthenticationException;
import com.ticket.reservation.model.User;
import com.ticket.reservation.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@email.com", "1234567890", "hashedPassword");
        testUser.setId("user123");
    }

    // ==================== Login Endpoint Tests ====================

    @Test
    void login_successWithEmail() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("test@email.com");
        request.setPassword("password123");

        when(userService.login("test@email.com", "password123")).thenReturn(testUser);
        when(userService.generateToken(testUser)).thenReturn("mockJwtToken");

        ResponseEntity<LoginResponseDTO> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LoginResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("user123", body.getId());
        assertEquals("Test User", body.getName());
        assertEquals("test@email.com", body.getEmail());
        assertEquals("1234567890", body.getPhone());
        assertEquals("mockJwtToken", body.getToken());
    }

    @Test
    void login_successWithPhone() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("1234567890");
        request.setPassword("password123");

        User phoneUser = new User("Phone User", null, "1234567890", "hashedPassword");
        phoneUser.setId("user456");

        when(userService.login("1234567890", "password123")).thenReturn(phoneUser);
        when(userService.generateToken(phoneUser)).thenReturn("mockJwtToken");

        ResponseEntity<LoginResponseDTO> response = userController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LoginResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("user456", body.getId());
        assertEquals("Phone User", body.getName());
        assertEquals("1234567890", body.getPhone());
        assertEquals("mockJwtToken", body.getToken());
    }

    @Test
    void login_userNotFound_throwsAuthenticationException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("nonexistent@email.com");
        request.setPassword("password123");

        when(userService.login("nonexistent@email.com", "password123"))
                .thenThrow(new AuthenticationException("User not found with provided identifier."));

        AuthenticationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                AuthenticationException.class,
                () -> userController.login(request));

        assertEquals("User not found with provided identifier.", exception.getMessage());
    }

    @Test
    void login_invalidPassword_throwsAuthenticationException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentifier("test@email.com");
        request.setPassword("wrongPassword");

        when(userService.login("test@email.com", "wrongPassword"))
                .thenThrow(new AuthenticationException("Invalid password."));

        AuthenticationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                AuthenticationException.class,
                () -> userController.login(request));

        assertEquals("Invalid password.", exception.getMessage());
    }
}
