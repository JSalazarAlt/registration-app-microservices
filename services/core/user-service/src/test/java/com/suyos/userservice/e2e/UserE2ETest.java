package com.suyos.userservice.e2e;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserProfileResponse;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

/**
 * End-to-end tests for complete user workflows.
 *
 * <p>Tests full user lifecycle from creation to search with real
 * HTTP requests and database persistence.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserE2ETest {
    
    /** REST template for HTTP requests */
    @Autowired
    private TestRestTemplate restTemplate;
    
    /** User repository for test data setup */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Tests complete user lifecycle from creation to search.
     */
    @Test
    void completeUserLifecycle() {
        UUID accountId = UUID.randomUUID();
        
        // Create user
        User user = User.builder()
            .accountId(accountId)
            .username("e2euser")
            .email("e2e@example.com")
            .firstName("E2E")
            .lastName("Test")
            .phone("1234567890")
            .termsAcceptedAt(Instant.now())
            .privacyPolicyAcceptedAt(Instant.now())
            .build();
        user = userRepository.save(user);
        
        // Get user by ID
        ResponseEntity<UserProfileResponse> getResponse = restTemplate.getForEntity(
            "/api/v1/users/" + user.getId(),
            UserProfileResponse.class
        );
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("e2euser", getResponse.getBody().getUsername());
        
        // Update user
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
            .firstName("Updated")
            .lastName("Name")
            .build();
        
        ResponseEntity<UserProfileResponse> updateResponse = restTemplate.exchange(
            "/api/v1/users/" + user.getId(),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            UserProfileResponse.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated", updateResponse.getBody().getFirstName());
        
        // Search user
        ResponseEntity<UserProfileResponse[]> searchResponse = restTemplate.getForEntity(
            "/api/v1/users/search?name=Updated",
            UserProfileResponse[].class
        );
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertTrue(searchResponse.getBody().length > 0);
    }
}
