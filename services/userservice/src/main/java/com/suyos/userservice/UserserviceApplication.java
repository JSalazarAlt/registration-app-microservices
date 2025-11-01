package com.suyos.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the User microservice.
 *
 * <p>Manages user profiles, personal information, and user-related data
 * operations. Works in conjunction with Authentication service to provide
 * complete user management functionality.</p>
 *
 * @author Joel Salazar
 */
@SpringBootApplication
@EnableJpaAuditing
public class UserserviceApplication {

	/**
	 * Main method to start the User microservice.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}

}
