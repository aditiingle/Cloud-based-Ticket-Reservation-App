package com.ticket.reservation.controller;

import com.ticket.reservation.model.User;
import com.ticket.reservation.dto.UserRegistrationRequestDTO;
import com.ticket.reservation.dto.UserRegistrationResponseDTO;
import com.ticket.reservation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRegistrationRequestDTO request) {
        try {
            User user = new User(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword()
            );

            User savedUser = userService.createUser(user);

            UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO(savedUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
