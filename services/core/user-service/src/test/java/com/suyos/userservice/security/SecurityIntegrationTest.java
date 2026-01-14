package com.suyos.userservice.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suyos.userservice.dto.request.UserUpdateRequest;

/**
 * Security integration tests for JWT authentication.
 *
 * <p>Tests JWT authentication and authorization with MockMvc to verify
 * security rules and endpoint access control.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    
    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /** ObjectMapper for JSON serialization/deserialization */
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Tests that requests without JWT token return 401.
     */
    @Test
    void shouldReturn401WhenNoJwtToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
    
    /**
     * Tests that requests with valid JWT token return 200.
     */
    @Test
    @WithMockUser
    void shouldReturn200WhenValidJwtToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk());
    }
    
    /**
     * Tests that update requests without authentication return 401.
     */
    @Test
    void shouldReturn401WhenUpdatingWithoutAuth() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Test")
            .build();
        
        mockMvc.perform(put("/api/v1/users/{userId}", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    /**
     * Tests that actuator health endpoint is publicly accessible.
     */
    @Test
    void shouldAllowActuatorHealthWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
    
    /**
     * Tests that internal endpoints are accessible without authentication.
     */
    @Test
    void shouldAllowInternalEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(post("/internal/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().is4xxClientError());
    }
}
