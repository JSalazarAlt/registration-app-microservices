package com.suyos.userservice.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

/**
 * Integration tests for UserController.
 *
 * <p>Tests user profile endpoints with full Spring context and
 * database integration to verify end-to-end functionality.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {
    
    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /** ObjectMapper for JSON serialization/deserialization */
    @Autowired
    private ObjectMapper objectMapper;
    
    /** User repository for test data setup */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Tests successful user retrieval by ID.
     */
    @Test
    @WithMockUser
    void shouldGetUserById() throws Exception {
        User user = User.builder()
            .accountId(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .termsAcceptedAt(Instant.now())
            .privacyPolicyAcceptedAt(Instant.now())
            .build();
        
        User saved = userRepository.save(user);
        
        mockMvc.perform(get("/api/v1/users/{userId}", saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    /**
     * Tests successful user update.
     */
    @Test
    @WithMockUser
    void shouldUpdateUser() throws Exception {
        User user = User.builder()
            .accountId(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .firstName("Original")
            .lastName("Name")
            .termsAcceptedAt(Instant.now())
            .privacyPolicyAcceptedAt(Instant.now())
            .build();
        
        User saved = userRepository.save(user);
        
        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Updated")
            .build();
        
        mockMvc.perform(put("/api/v1/users/{userId}", saved.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Updated"));
    }
}
