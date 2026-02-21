package com.ticket.reservation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserTest {
    @Test
    void testUserGettersAndSetters() {
        User user = new User();
        user.setName("Aditi");
        user.setEmail("aditi@email.com");

        assertEquals("Aditi", user.getName());
        assertEquals("aditi@email.com", user.getEmail());
    }

    @Test
    void testUserConstructor() {
        User user = new User("Aditi", "aditi@email.com");

        assertEquals("Aditi", user.getName());
        assertEquals("aditi@email.com", user.getEmail());
        assertNull(user.getId());
    }

}
