package com.suyos.authservice.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.suyos.authservice.model.Account;

/**
 * Repository interface for Account entity data access operations.
 * 
 * <p>Extends JpaRepository to provide standard CRUD operations and
 * authentication-specific query methods for Account entities. Spring Data
 * JPA automatically generates the implementation at runtime.</p>
 * 
 * <p>Additional operations include login validation, account security
 * management, and user verification queries.</p>
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

    /**
     * Finds an account by ID.
     * 
     * @param id Account ID to search for
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByID(UUID id);

    /**
     * Finds an account by email address.
     * 
     * @param email Email address to search for
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByEmail(String email);

    /**
     * Finds an account by username.
     * 
     * @param username Username to search for
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByUsername(String username);

    /**
     * Finds an account by OAuth2 provider and provider ID.
     * 
     * @param provider OAuth2 provider name (e.g., "google")
     * @param providerId Unique identifier from OAuth2 provider
     * @return Optional containing account if found, empty otherwise
     */
    Optional<Account> findByOauth2ProviderAndOauth2ProviderId(String provider, String providerId);

    /**
     * Finds an active account by ID (enabled, not locked, not deleted).
     * 
     * @param id Account ID to search for
     * @return Optional containing active account if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.id = :id 
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveById(@Param("id") UUID id);

    /**
     * Finds an active account by email (enabled, not locked, not deleted).
     * 
     * @param email Email address to search for
     * @return Optional containing active account if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.email = :email 
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveByEmail(@Param("email") String email);

    /**
     * Finds an active account by username (enabled, not locked, not deleted).
     * 
     * @param username Username to search for
     * @return Optional containing active account if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.username = :username 
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveByUsername(@Param("username") String username);

    /**
     * Finds an active account by OAuth2 provider and provider ID.
     * 
     * @param provider OAuth2 provider name (e.g., "google")
     * @param providerId Unique identifier from OAuth2 provider
     * @return Optional containing active account if found, empty
     * otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.oauth2Provider = :provider AND a.oauth2ProviderId = :providerId
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveByOauth2ProviderAndOauth2ProviderId(
        String provider, String providerId);

    /**
     * Locks a user account until the specified time.
     * 
     * @param email Email of the user to lock
     * @param lockedUntil Timestamp when the lock expires
     */
    /*
    @Modifying
    @Query("""
        UPDATE Account a
        SET a.locked = true, a.lockedUntil = :lockedUntil
        WHERE a.email = :email
    """)
    void lockAccount(@Param("email") String email, @Param("lockedUntil") LocalDateTime lockedUntil);
    */

    /**
     * Unlocks a user account and resets failed login attempts.
     * 
     * @param email Email of the user to unlock
     */
    /*
    @Modifying
    @Query("""
        UPDATE Account a
        SET a.locked = false, a.lockedUntil = null, a.failedLoginAttempts = 0
        WHERE a.email = :email
    """)
    void unlockAccount(@Param("email") String email);
    */
    
}
