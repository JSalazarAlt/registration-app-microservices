package com.suyos.userservice.integration.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

/**
 * Integration tests for UserRepository.
 *
 * <p>Tests JPA repository methods with in-memory database to verify
 * data access operations.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {
    
    /** User repository under test */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Tests user persistence and retrieval.
     */
    @Test
    void shouldPersistAndRetrieveUser() {
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
        
        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }
    
    /**
     * Tests user retrieval by account ID.
     */
    @Test
    void shouldFindByAccountId() {
        UUID accountId = UUID.randomUUID();
        User user = User.builder()
            .accountId(accountId)
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .termsAcceptedAt(Instant.now())
            .privacyPolicyAcceptedAt(Instant.now())
            .build();
        
        userRepository.save(user);
        
        assertThat(userRepository.findByAccountId(accountId)).isPresent();
    }
}
