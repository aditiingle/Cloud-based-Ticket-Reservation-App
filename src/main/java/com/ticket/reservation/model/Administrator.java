package com.ticket.reservation.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "User")
public class Administrator extends User {

    public Administrator() {
        super();
    }

    public Administrator(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }
}
