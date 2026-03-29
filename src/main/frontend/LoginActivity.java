package com.ticket.reservation.frontend;

public class LoginActivity {

    private String email;
    private String password;

    public LoginActivity(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean validate() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
