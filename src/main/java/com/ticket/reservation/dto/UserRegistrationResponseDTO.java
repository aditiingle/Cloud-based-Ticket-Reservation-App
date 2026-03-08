package com.ticket.reservation.dto;

import com.ticket.reservation.model.User;

public class UserRegistrationResponseDTO {
    private String id;
    private String name;
    private String email;
    private String phone;

    public UserRegistrationResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
