package com.ticket.reservation.service;

import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository; // Mocked repository

    @InjectMocks
    private UserService userService; // Inject mocked dependencies and create UserService instance

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize all @Mock and @InjectMocks annotations before each test
    }

    @Test
    void createUser_success() {

        // Arrange: create a new user and mock repository to return empty (no duplicates)
        User user = new User("Test Name", "test@email.com", "1234567890");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        // Act: call the method in UserService
        User created = userService.createUser(user);

        // Assert: check returned user and verify save was called once
        assertNotNull(created);
        assertEquals("test@email.com", created.getEmail());
        assertEquals("1234567890", created.getPhone());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_duplicateEmail_throwsException() {

        // Arrange: user email already exists in repository
        User user = new User("Duplicate Email User", "duplicate@email.com", "1111111111");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(new User()));
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.empty());

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        // Assert
        assertEquals("Email already registered.", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void createUser_duplicatePhone_throwsException() {

        // Arrange: user phone already exists
        User user = new User("Duplicate Phone User","new@email.com", "9999999999");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(new User()));

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        // Assert
        assertEquals("Phone number already registered.", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void createUser_missingEmailAndPhone_throwsException() {

        // Arrange: user has neither email nor phone
        User user = new User("Test Name", null, null);

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        //Assert
        assertEquals("Email or phone must be provided.", exception.getMessage());
        verify(userRepository, never()).save(user);
    }
}
