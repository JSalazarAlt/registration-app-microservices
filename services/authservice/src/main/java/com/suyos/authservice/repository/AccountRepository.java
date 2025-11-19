package com.suyos.authservice.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.model.Account;

/**
 * Repository for account data access operations.
 * 
 * <p>Provides standard CRUD operations for account entities and specific
 * query methods.</p>
 * 
 * @author Joel Salazar
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    // ----------------------------------------------------------------
    // EXISTENCE CHECKS
    // ----------------------------------------------------------------

    /**
     * Checks if an email address is already registered.
     * 
     * @param email Email address to check
     * @return True if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Checks if a username is already taken.
     * 
     * @param username Username to check
     * @return True if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds an account by username.
     * 
     * @param username Username to search for
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByUsername(String username);

    /**
     * Finds an account by email address.
     * 
     * @param email Email address to search for
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByEmail(String email);

    /**
     * Finds an account by OAuth2 provider and provider ID.
     * 
     * @param provider OAuth2 provider name (e.g., "google")
     * @param providerId Unique identifier from OAuth2 provider
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByOauth2ProviderAndOauth2ProviderId(String provider, String providerId);

    // ----------------------------------------------------------------
    // LOCK AND UNLOCK
    // ----------------------------------------------------------------

    @Modifying
    @Transactional
    @Query("""
        UPDATE Account a
        SET a.locked = true, a.lockedUntil = :until 
        WHERE a.id = :id
    """)
    int lockAccount(UUID id, LocalDateTime until);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Account a
        SET a.locked = false, a.lockedUntil = null 
        WHERE a.id = :id
    """)
    int unlockAccount(UUID id);

}