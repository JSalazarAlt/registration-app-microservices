package com.suyos.userservice.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.suyos.userservice.model.User;

/**
 * Unit tests for UserRepository.
 *
 * <p>Tests JPA repository methods for user data access operations
 * using in-memory database.</p>
 *
 * @author Joel Salazar
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    /** User repository under test */
    @Autowired
    private UserRepository userRepository;
    
    /** Test user entity */
    private User testUser;
    
    /** Test account ID */
    private UUID accountId;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test account ID
        accountId = UUID.randomUUID();
        
        // Build and save test user
        testUser = User.builder()
                .accountId(accountId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .termsAcceptedAt(LocalDateTime.now())
                .privacyPolicyAcceptedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
    }

    /**
     * Tests successful user retrieval by account ID.
     */
    @Test
    void findByAccountId_Success() {
        Optional<User> result = userRepository.findByAccountId(accountId);
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
    }

    /**
     * Tests that findByAccountId returns empty for non-existing
     * account ID.
     */
    @Test
    void findByAccountId_NotFound() {
        UUID nonExistentAccountId = UUID.randomUUID();
        Optional<User> result = userRepository.findByAccountId(nonExistentAccountId);
        assertFalse(result.isPresent());
    }

    /**
     * Tests successful user search by first name.
     */
    @Test
    void findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_FirstName() {
        List<User> result = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("test", "test");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getFirstName());
    }

    /**
     * Tests successful user search by last name.
     */
    @Test
    void findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_LastName() {
        List<User> result = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("user", "user");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("User", result.get(0).getLastName());
    }

    /**
     * Tests case-insensitive user search.
     */
    @Test
    void findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_CaseInsensitive() {
        List<User> result = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("TEST", "USER");
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    /**
     * Tests that search returns empty list for non-matching name.
     */
    @Test
    void findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase_NotFound() {
        List<User> result = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("nonexistent", "nonexistent");
        assertTrue(result.isEmpty());
    }

    /**
     * Tests successful user creation.
     */
    @Test
    void saveUser_Success() {
        // Build new user
        User newUser = User.builder()
                .accountId(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .firstName("New")
                .lastName("User")
                .termsAcceptedAt(LocalDateTime.now())
                .privacyPolicyAcceptedAt(LocalDateTime.now())
                .build();
        
        // Save user
        User savedUser = userRepository.save(newUser);
        
        // Verify user was saved
        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("new@example.com", savedUser.getEmail());
    }

    /**
     * Tests successful user deletion.
     */
    @Test
    void deleteUser_Success() {
        // Delete user
        UUID userId = testUser.getId();
        userRepository.deleteById(userId);
        
        // Verify user was deleted
        Optional<User> result = userRepository.findById(userId);
        assertFalse(result.isPresent());
    }
}