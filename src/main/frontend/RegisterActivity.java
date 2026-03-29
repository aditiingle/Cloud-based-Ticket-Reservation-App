package com.ticket.reservation.frontend;

public class RegisterActivity {

    private String email;
    private String phone;
    private String password;
    private String confirmPassword;

    public RegisterActivity(String email, String phone, String password, String confirmPassword) {
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public boolean validate() {
        if ((email == null || email.trim().isEmpty()) &&
            (phone == null || phone.trim().isEmpty())) {
            return false; // need at least email or phone
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        if (!password.equals(confirmPassword)) {
            return false; // passwords must match
        }
        return true;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }
}
