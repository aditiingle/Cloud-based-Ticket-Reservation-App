package com.ticket.reservation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.reservation.dto.LoginRequestDTO;
import com.ticket.reservation.dto.LoginResponseDTO;
import com.ticket.reservation.dto.UserRegistrationRequestDTO;
import com.ticket.reservation.dto.UserRegistrationResponseDTO;
import com.ticket.reservation.exception.AuthenticationException;
import com.ticket.reservation.model.User;
import com.ticket.reservation.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Operations related to users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Register a new user by phone or email")
    public ResponseEntity<UserRegistrationResponseDTO> createUser(@Valid @RequestBody UserRegistrationRequestDTO request) {
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );

        User savedUser = userService.createUser(user);
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/phone and password")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = userService.login(request.getIdentifier(), request.getPassword());
        String token = userService.generateToken(user);
        LoginResponseDTO responseDTO = new LoginResponseDTO(user, token);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserRegistrationResponseDTO> getCurrentUser(@AuthenticationPrincipal String email) {
        if (email == null) {
            throw new AuthenticationException("User not authenticated");
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new AuthenticationException("User not found");
        }
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO(user);
        return ResponseEntity.ok(responseDTO);
    }

}
