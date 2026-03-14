package com.ticket.reservation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ticket.reservation.exception.AuthenticationException;
import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.UserRepository;
import com.ticket.reservation.util.JwtUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if ((user.getEmail() == null || user.getEmail().isEmpty()) &&
                (user.getPhone() == null || user.getPhone().isEmpty())) {
            throw new IllegalArgumentException("Email or phone must be provided.");
        }

        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered.");
        }

        if (user.getPhone() != null && userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered.");
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        } else {
            throw new IllegalArgumentException("Password must be provided.");
        }


        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User login(String identifier, String password) {
        // Try to find user by email or phone
        Optional<User> userOptional = userRepository.findByEmail(identifier);
        
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByPhone(identifier);
        }
        
        if (userOptional.isEmpty()) {
            throw new AuthenticationException("User not found with provided identifier.");
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid password.");
        }
        
        return user;
    }
    
    public String generateToken(User user) {
        String email = user.getEmail() != null ? user.getEmail() : user.getPhone();
        return jwtUtil.generateToken(user.getId(), email);
    }
}