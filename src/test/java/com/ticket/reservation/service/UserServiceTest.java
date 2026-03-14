package com.ticket.reservation.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ticket.reservation.exception.AuthenticationException;
import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.UserRepository;
import com.ticket.reservation.util.JwtUtil;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_success() {

        // Arrange: create a new user and mock repository to return empty (no duplicates)
        User user = new User("Test Name", "test@email.com", "1234567890", "testPassword");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testPassword")).thenReturn("$2a$10$hashedPassword");
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
        User user = new User("Duplicate Email User", "duplicate@email.com", "1111111111", "testPassword");
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
        User user = new User("Duplicate Phone User","new@email.com", "9999999999", "testPassword");
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
        User user = new User("Test Name", null, null, "testPassword");

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        //Assert
        assertEquals("Email or phone must be provided.", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void testPasswordIsHashed() {
        // Arrange:
        String plainPassword = "MySecret123!";
        String hashedPassword = "$2a$10$hashedPasswordValue";
        User user = new User("Alice", "alice@email.com", "+123456789", plainPassword);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(plainPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = userService.createUser(user);

        // Assert
        assertNotEquals(plainPassword, savedUser.getPassword(), "Password should be hashed and not equal to plain text");
        //assertTrue(passwordEncoder.matches(plainPassword, savedUser.getPassword()), "Hashed password should match the plain password");
        assertEquals(hashedPassword, savedUser.getPassword());
        verify(passwordEncoder, times(1)).encode(plainPassword);
    }

    // ==================== Login Tests ====================

    @Test
    void login_successWithEmail() {
        String plainPassword = "password123";
        String hashedPassword = "$2a$10$hashedPassword";
        User user = new User("Test User", "test@email.com", "1234567890", hashedPassword);
        user.setId("user123");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(plainPassword, hashedPassword)).thenReturn(true);

        User result = userService.login("test@email.com", plainPassword);

        assertNotNull(result);
        assertEquals("test@email.com", result.getEmail());
        assertEquals("Test User", result.getName());

        verify(userRepository, times(1)).findByEmail("test@email.com");
        verify(userRepository, never()).findByPhone(anyString());
    }

    @Test
    void login_successWithPhone() {
        String plainPassword = "password123";
        String hashedPassword = "$2a$10$hashedPassword";
        User user = new User("Test User", null, "1234567890", hashedPassword);
        user.setId("user123");

        when(userRepository.findByEmail("1234567890")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(plainPassword, hashedPassword)).thenReturn(true);

        User result = userService.login("1234567890", plainPassword);

        assertNotNull(result);
        assertEquals("1234567890", result.getPhone());
        assertEquals("Test User", result.getName());

        verify(userRepository, times(1)).findByEmail("1234567890");
        verify(userRepository, times(1)).findByPhone("1234567890");
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByEmail("nonexistent@email.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("nonexistent@email.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(AuthenticationException.class, () -> {
            userService.login("nonexistent@email.com", "password123");
        });

        assertEquals("User not found with provided identifier.", exception.getMessage());
    }

    @Test
    void login_invalidPassword_throwsException() {
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = "$2a$10$hashedPassword";
        User user = new User("Test User", "test@email.com", "1234567890", hashedPassword);

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, hashedPassword)).thenReturn(false);

        Exception exception = assertThrows(AuthenticationException.class, () -> {
            userService.login("test@email.com", wrongPassword);
        });

        assertEquals("Invalid password.", exception.getMessage());
    }

    // ==================== Token Generation Tests ====================

    @Test
    void generateToken_success() {
        User user = new User("Test User", "test@email.com", "1234567890", "hashedPassword");
        user.setId("user123");

        when(jwtUtil.generateToken("user123", "test@email.com")).thenReturn("mockToken");

        String token = userService.generateToken(user);

        assertNotNull(token);
        assertEquals("mockToken", token);
        verify(jwtUtil, times(1)).generateToken("user123", "test@email.com");
    }

    @Test
    void generateToken_usesPhoneWhenEmailIsNull() {
        User user = new User("Test User", null, "1234567890", "hashedPassword");
        user.setId("user123");

        when(jwtUtil.generateToken("user123", "1234567890")).thenReturn("mockToken");

        String token = userService.generateToken(user);

        assertNotNull(token);
        assertEquals("mockToken", token);
        verify(jwtUtil, times(1)).generateToken("user123", "1234567890");
    }
}
