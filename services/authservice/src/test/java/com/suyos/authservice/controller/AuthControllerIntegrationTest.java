package com.suyos.authservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;

/**
 * Integration tests for AuthController.
 *
 * <p>Tests authentication endpoints with full Spring context and
 * database integration to verify end-to-end functionality.</p>
 *
 * @author Joel Salazar
 */
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /** ObjectMapper for JSON serialization/deserialization */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests successful account registration with database persistence.
     * 
     * <p>Verifies that registration creates account in database and
     * returns 201 Created with account information.</p>
     */
    @Test
    void registerAccount_Success() throws Exception {
        // Build registration request
        RegistrationRequestDTO registrationDTO = RegistrationRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        // Perform registration and verify response
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Tests registration with invalid email format.
     * 
     * <p>Verifies that registration fails with 400 Bad Request when
     * email format is invalid.</p>
     */
    @Test
    void registerAccount_InvalidEmail() throws Exception {
        // Build registration request with invalid email
        RegistrationRequestDTO registrationDTO = RegistrationRequestDTO.builder()
                .username("testuser")
                .email("invalid-email")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        // Perform registration and expect bad request
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests successful login with valid credentials.
     * 
     * <p>Verifies that login returns 200 OK with tokens after
     * registering and authenticating a user.</p>
     */
    @Test
    void loginAccount_Success() throws Exception {
        // Register a user first
        RegistrationRequestDTO registrationDTO = RegistrationRequestDTO.builder()
                .username("logintest")
                .email("login@example.com")
                .password("password123")
                .firstName("Login")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)));

        // Build login request
        AuthenticationRequestDTO loginDTO = AuthenticationRequestDTO.builder()
                .identifier("login@example.com")
                .password("password123")
                .build();

        // Perform login and verify tokens are returned
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accountId").exists());
    }

    /**
     * Tests login with invalid credentials.
     * 
     * <p>Verifies that login fails with 401 Unauthorized when
     * credentials are incorrect or account does not exist.</p>
     */
    @Test
    void loginAccount_InvalidCredentials() throws Exception {
        // Build login request with invalid credentials
        AuthenticationRequestDTO loginDTO = AuthenticationRequestDTO.builder()
                .identifier("nonexistent@example.com")
                .password("wrongpassword")
                .build();

        // Perform login and expect unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }
    
}