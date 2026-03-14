package com.ticket.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

import com.ticket.reservation.model.User;
import com.ticket.reservation.service.UserService;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ReservationApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    // Set to true to seed initial data on startup
    private static final boolean SEED_DATA = false;

	public static void main(String[] args) {
		SpringApplication.run(ReservationApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        if (SEED_DATA) {
            User u = new User("El", "el@email.com", null, "password123");
            userService.createUser(u);

            userService.getAllUsers().forEach(user ->
                    System.out.println(user.getName() + " - " + user.getEmail())
            );
        }
    }

}
