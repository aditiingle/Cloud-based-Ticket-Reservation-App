package com.ticket.reservation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ticket.reservation.dto.UserRegistrationRequestDTO;
import com.ticket.reservation.dto.UserRegistrationResponseDTO;
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

    @Test
    void createUser_success() {
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setName("User");
        request.setEmail("user@email.com");
        request.setPhone("1234567890");
        request.setPassword("password123");

        User savedUser = new User("User", "user@email.com", "1234567890", "hashedPassword");
        savedUser.setId("user789");

        when(userService.createUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);

        ResponseEntity<UserRegistrationResponseDTO> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserRegistrationResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("user789", body.getId());
        assertEquals("User", body.getName());
        assertEquals("user@email.com", body.getEmail());
        assertEquals("1234567890", body.getPhone());
    }

    @Test
    void createUser_duplicateEmail_throwsIllegalArgumentException() {
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setName("User");
        request.setEmail("user@email.com");
        request.setPhone("1234567890");
        request.setPassword("password123");

        when(userService.createUser(org.mockito.ArgumentMatchers.any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already registered."));

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userController.createUser(request));

        assertEquals("Email already registered.", exception.getMessage());
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

    @Test
    void getCurrentUser_success() {
        when(userService.getUserByEmail("test@email.com")).thenReturn(testUser);

        ResponseEntity<UserRegistrationResponseDTO> response = userController.getCurrentUser("test@email.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserRegistrationResponseDTO body = response.getBody();
        assertNotNull(body);
        assertEquals("user123", body.getId());
        assertEquals("Test User", body.getName());
        assertEquals("test@email.com", body.getEmail());
        assertEquals("1234567890", body.getPhone());
    }

    @Test
    void getCurrentUser_notAuthenticated_throwsAuthenticationException() {
        AuthenticationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                AuthenticationException.class,
                () -> userController.getCurrentUser(null));

        assertEquals("User not authenticated", exception.getMessage());
    }
}
