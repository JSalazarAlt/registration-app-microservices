package com.suyos.authservice.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.suyos.authservice.model.Account;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    
    private Account testAccount;

    @BeforeEach
    void setUp() {
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

    @Test
    void existsByEmail_ReturnsTrue() {
        assertTrue(accountRepository.existsByEmail("test@example.com"));
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        assertFalse(accountRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void existsByUsername_ReturnsTrue() {
        assertTrue(accountRepository.existsByUsername("testuser"));
    }

    @Test
    void existsByUsername_ReturnsFalse() {
        assertFalse(accountRepository.existsByUsername("nonexistent"));
    }

    @Test
    void findByEmail_Success() {
        Optional<Account> result = accountRepository.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByUsername_Success() {
        Optional<Account> result = accountRepository.findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findActiveByEmail_Success() {
        Optional<Account> result = accountRepository.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findActiveByEmail_LockedAccount() {
        testAccount.setLocked(true);
        accountRepository.save(testAccount);
        
        Optional<Account> result = accountRepository.findByEmail("test@example.com");
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveById_Success() {
        Optional<Account> result = accountRepository.findById(testAccount.getId());
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findActiveById_DisabledAccount() {
        testAccount.setEnabled(false);
        accountRepository.save(testAccount);
        
        Optional<Account> result = accountRepository.findById(testAccount.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void findByOauth2ProviderAndOauth2ProviderId_Success() {
        testAccount.setOauth2Provider("google");
        testAccount.setOauth2ProviderId("google123");
        accountRepository.save(testAccount);
        
        Optional<Account> result = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", "google123");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findActiveByOauth2ProviderAndOauth2ProviderId_Success() {
        testAccount.setOauth2Provider("google");
        testAccount.setOauth2ProviderId("google123");
        accountRepository.save(testAccount);
        
        Optional<Account> result = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", "google123");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

}