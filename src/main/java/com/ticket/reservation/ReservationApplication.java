package com.ticket.reservation;

import com.ticket.reservation.model.User;
import com.ticket.reservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ReservationApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(ReservationApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
//        User u = new User("Mike", "mike@email.com");
//        userRepository.save(u);
//
//        userRepository.findAll().forEach(user ->
//                System.out.println(user.getName() + " - " + user.getEmail())
//        );
    }

}
