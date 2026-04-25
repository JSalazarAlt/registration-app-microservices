package com.suyos.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Authentication microservice.
 *
 * <p>Handles user authentication, account management, token generation,
 * and OAuth2 integration. Provides secure authentication endpoints for
 * the application ecosystem.</p>
 */
@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}