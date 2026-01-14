package com.suyos.userservice.integration.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for security configuration.
 *
 * <p>Tests JWT authentication and authorization with MockMvc to verify
 * security rules and endpoint access control.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {
    
    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /**
     * Tests that unauthenticated requests return 401.
     */
    @Test
    void shouldReturn401WhenNoAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
    
    /**
     * Tests that authenticated requests return 200.
     */
    @Test
    @WithMockUser
    void shouldReturn200WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk());
    }
    
    /**
     * Tests that actuator health endpoint is publicly accessible.
     */
    @Test
    void shouldAllowActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
}
