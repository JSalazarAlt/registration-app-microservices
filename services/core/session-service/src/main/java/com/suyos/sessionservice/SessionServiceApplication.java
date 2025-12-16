package com.suyos.sessionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SessionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SessionServiceApplication.class, args);
	}

}
