package com.ticket.reservation.service;

import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}