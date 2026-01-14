package com.suyos.userservice.integration.service;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserProfileResponse;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;
import com.suyos.userservice.service.UserService;

/**
 * Integration tests for UserService.
 *
 * <p>Tests service layer with full Spring context and database
 * integration to verify transactional behavior.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {
    
    /** User service under test */
    @Autowired
    private UserService userService;
    
    /** User repository for test data setup */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Tests user update with transaction management.
     */
    @Test
    void shouldUpdateUserWithTransaction() {
        UUID accountId = UUID.randomUUID();
        User user = User.builder()
            .accountId(accountId)
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
            .lastName("NewName")
            .build();
        
        UserProfileResponse updated = userService.updateUserById(saved.getId(), request);
        
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getLastName()).isEqualTo("NewName");
    }
}
