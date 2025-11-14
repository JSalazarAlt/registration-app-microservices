package com.suyos.authservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suyos.authservice.model.Account;

/**
 * Repository for account data access operations.
 * 
 * <p>Provides standard CRUD operations for account entities.</p>
 * 
 * @author Joel Salazar
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
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

    // ACCOUNT LOOKUP

    /**
     * Finds an account by ID.
     * 
     * @param id Account ID to search for
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByID(UUID id);

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
    
}