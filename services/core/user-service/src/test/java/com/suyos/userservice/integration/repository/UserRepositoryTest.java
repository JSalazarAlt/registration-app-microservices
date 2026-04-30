package com.suyos.userservice.integration.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

/**
 * Unit tests for UserRepository.
 *
 * <p>Tests JPA repository methods for user data access operations using
 * in-memory database.</p>
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
    private UUID testAccountId;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test account ID
        testAccountId = UUID.randomUUID();

        // Create test user
        testUser = createTestUser(
            testAccountId, 
            "testuser", 
            "test@example.com",
            "Test",
            "User"
        );

        // Persist test user
        testUser = userRepository.save(testUser);
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    /**
     * Creates a test user with the provided details.
     *
     * @param accountId Account ID
     * @param username Username
     * @param email Email address
     * @param firstName First name
     * @param lastName Last name
     * @return User entity
     */
    private User createTestUser(UUID accountId, String username, String email, String firstName, String lastName) {
        User testUser = User.builder()
                .accountId(accountId)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("1234567890")
                .termsAcceptedAt(Instant.now())
                .privacyPolicyAcceptedAt(Instant.now())
                .build();
        return testUser;
    }

    // ----------------------------------------------------------------
    // LOOKUP TESTS
    // ----------------------------------------------------------------

    /**
     * Tests successful user retrieval by account ID.
     */
    @Test
    @DisplayName("Should find user by account ID")
    void findByAccountId_Success() {
        // Find user by account ID
        Optional<User> result = userRepository.findByAccountId(testAccountId);
        
        // Verify user is found and has expected properties
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                });
    }

    /**
     * Tests that findByAccountId returns empty for non-existing account ID.
     */
    @Test
    @DisplayName("Should return empty Optional when account ID not found")
    void findByAccountId_NotFound() {
        // Generate a non-existent account ID
        UUID nonExistentAccountId = UUID.randomUUID();

        // Find user by account ID
        Optional<User> result = userRepository.findByAccountId(nonExistentAccountId);

        // Verify no user is found
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // SAVE TESTS
    // ----------------------------------------------------------------

    /**
     * Tests successful user creation and persistence.
     */
    @Test
    @DisplayName("Should successfully create and persist a new user")
    void saveUser_Success() {
        // Create new test user
        User newUser = createTestUser(
            UUID.randomUUID(), 
            "newuser",
            "new@example.com", 
            "New", 
            "User"
        );

        // Persist new test user
        User savedUser = userRepository.save(newUser);

        // Verify new test user is persisted
        assertThat(savedUser)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getId()).isNotNull();
                    assertThat(user.getUsername()).isEqualTo("newuser");
                    assertThat(user.getEmail()).isEqualTo("new@example.com");
                    assertThat(user.getFirstName()).isEqualTo("New");
                });
    }

    /**
     * Tests that user can be persisted without phone number (null field).
     */
    @Test
    @DisplayName("Should persist user without phone number")
    void saveUser_WithoutphoneNumber() {
        // Create new test user without phone number
        User newUser = User.builder()
                .accountId(UUID.randomUUID())
                .username("phoneuser")
                .email("phone@example.com")
                .firstName("Phone")
                .lastName("Test")
                .termsAcceptedAt(Instant.now())
                .privacyPolicyAcceptedAt(Instant.now())
                .build();

        // Persist new test user
        User savedUser = userRepository.save(newUser);

        // Verify new test user is persisted and phone number is null
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getPhoneNumber()).isNull();
    }

    // ----------------------------------------------------------------
    // DELETE TESTS
    // ----------------------------------------------------------------=

    /**
     * Tests successful user deletion.
     */
    @Test
    @DisplayName("Should successfully delete an existing user")
    void deleteUser_Success() {
        // Get test user ID
        UUID userId = testUser.getId();

        // Delete user by ID
        userRepository.deleteById(userId);

        // Verify user is deleted
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Tests that user count decreases after deletion.
     */
    @Test
    @DisplayName("Should reduce user count after deletion")
    void deleteUser_VerifyCount() {
        // Get user count before deletion
        long countBefore = userRepository.count();

        // Delete test user
        userRepository.deleteById(testUser.getId());

        // Get user count after deletion
        long countAfter = userRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

}