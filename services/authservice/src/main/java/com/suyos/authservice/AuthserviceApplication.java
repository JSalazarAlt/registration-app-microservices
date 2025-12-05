package com.suyos.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Authentication microservice.
 *
 * <p>Handles user authentication, account management, token generation,
 * and OAuth2 integration. Provides secure authentication endpoints for
 * the application ecosystem.</p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class AuthserviceApplication {

	/**
	 * Main method to start the Authentication microservice.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthserviceApplication.class, args);
	}

}