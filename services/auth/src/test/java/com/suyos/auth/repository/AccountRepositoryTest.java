package com.suyos.auth.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.suyos.auth.model.Account;

/**
 * Unit tests for AccountRepository.
 *
 * <p>Tests JPA repository methods for account data access operations
 * using in-memory database.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    /** Account repository under test */
    @Autowired
    private AccountRepository accountRepository;
    
    /** Test account entity */
    private Account testAccount;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Build and save test account
        testAccount = Account.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .locked(false)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
        testAccount = accountRepository.save(testAccount);
    }

    /**
     * Tests that existsByEmail returns true for existing email.
     */
    @Test
    void existsByEmail_ReturnsTrue() {
        assertTrue(accountRepository.existsByEmail("test@example.com"));
    }

    /**
     * Tests that existsByEmail returns false for non-existing email.
     */
    @Test
    void existsByEmail_ReturnsFalse() {
        assertFalse(accountRepository.existsByEmail("nonexistent@example.com"));
    }

    /**
     * Tests that existsByUsername returns true for existing username.
     */
    @Test
    void existsByUsername_ReturnsTrue() {
        assertTrue(accountRepository.existsByUsername("testuser"));
    }

    /**
     * Tests that existsByUsername returns false for non-existing
     * username.
     */
    @Test
    void existsByUsername_ReturnsFalse() {
        assertFalse(accountRepository.existsByUsername("nonexistent"));
    }

    /**
     * Tests successful account retrieval by email.
     */
    @Test
    void findByEmail_Success() {
        Optional<Account> result = accountRepository.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    /**
     * Tests successful account retrieval by username.
     */
    @Test
    void findByUsername_Success() {
        Optional<Account> result = accountRepository.findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    /**
     * Tests successful active account retrieval by email.
     */
    @Test
    void findActiveByEmail_Success() {
        Optional<Account> result = accountRepository.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    /**
     * Tests that locked account is not returned by findByEmail.
     */
    @Test
    void findActiveByEmail_LockedAccount() {
        // Lock the account
        testAccount.setLocked(true);
        accountRepository.save(testAccount);
        
        // Verify locked account is not found
        Optional<Account> result = accountRepository.findByEmail("test@example.com");
        assertFalse(result.isPresent());
    }

    /**
     * Tests successful active account retrieval by ID.
     */
    @Test
    void findActiveById_Success() {
        Optional<Account> result = accountRepository.findById(testAccount.getId());
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    /**
     * Tests that disabled account is not returned by findById.
     */
    @Test
    void findActiveById_DisabledAccount() {
        // Disable the account
        testAccount.setEnabled(false);
        accountRepository.save(testAccount);
        
        // Verify disabled account is not found
        Optional<Account> result = accountRepository.findById(testAccount.getId());
        assertFalse(result.isPresent());
    }

    /**
     * Tests successful account retrieval by OAuth2 provider and ID.
     */
    @Test
    void findByOauth2ProviderAndOauth2ProviderId_Success() {
        // Set OAuth2 provider details
        testAccount.setOauth2Provider("google");
        testAccount.setOauth2ProviderId("google123");
        accountRepository.save(testAccount);
        
        // Verify account is found by OAuth2 credentials
        Optional<Account> result = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", "google123");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    /**
     * Tests successful active account retrieval by OAuth2 provider
     * and ID.
     */
    @Test
    void findActiveByOauth2ProviderAndOauth2ProviderId_Success() {
        // Set OAuth2 provider details
        testAccount.setOauth2Provider("google");
        testAccount.setOauth2ProviderId("google123");
        accountRepository.save(testAccount);
        
        // Verify active account is found by OAuth2 credentials
        Optional<Account> result = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", "google123");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

}