package com.ticket.reservation.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "User")
public class Customer extends User {

    public Customer() {
        super();
    }

    public Customer(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }
}
