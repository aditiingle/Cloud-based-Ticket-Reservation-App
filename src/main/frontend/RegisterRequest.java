package com.ticket.reservation.frontend;

public class RegisterRequest {
    private String email;
    private String phone;
    private String password;

    public RegisterRequest(String email, String phone, String password) {
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
}
