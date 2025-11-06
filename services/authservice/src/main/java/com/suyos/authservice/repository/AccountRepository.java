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
 * <p>This interface extends JpaRepository to provide standard CRUD operations and 
 * authentication-specific query methods for Account entities. Spring Data JPA
 * automatically generates the implementation at runtime.</p>
 * 
 * <p>Additional authentication operations include login validation, account security 
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
     * @param id ID of account to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<Account> findByID(UUID id);

    /**
     * Finds an account by email address.
     * 
     * @param email Email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<Account> findByEmail(String email);

    /**
     * Finds an account by username.
     * 
     * @param username Username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<Account> findByUsername(String username);

    /**
     * Finds a user by OAuth2 provider and provider ID.
     * 
     * Used to locate existing OAuth2 users during Google authentication flow.
     * 
     * @param provider The OAuth2 provider name (google)
     * @param providerId The unique identifier from the OAuth2 provider
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<Account> findByOauth2ProviderAndOauth2ProviderId(String provider, String providerId);

    /**
     * Finds an active account by ID.
     * 
     * <p>Returns user only if account is enabled, not locked, and email is verified.
     * Used for login validation to ensure account is in good standing.</p>
     * 
     * @param id ID of the account to search for
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.id = :id 
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveById(@Param("id") UUID id);

    /**
     * Finds an active account by email address.
     * 
     * <p>Returns account only if account is enabled and not locked. Used for login validation
     * to ensure account is in good standing.</p>
     * 
     * @param email Email address to search for
     * @return Optional containing the active account if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.email = :email 
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveByEmail(@Param("email") String email);

    /**
     * Finds an active account by username.
     * 
     * <p>Returns user only if account is enabled and not locked.</p>
     * 
     * @param email Email address to search for
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.username = :username 
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveByUsername(@Param("username") String username);

    /**
     * Finds an active user by OAuth2 provider and provider ID.
     * 
     * <p>Returns user only if account is enabled, not locked, and OAuth2 linked.
     * Recommended for OAuth2 authentication to ensure account security.</p>
     * 
     * @param provider The OAuth2 provider name (google)
     * @param providerId The unique identifier from the OAuth2 provider
     * @return Optional containing the active user if found, empty otherwise
     */
    @Query("""
        SELECT a FROM Account a 
        WHERE a.oauth2Provider = :provider AND a.oauth2ProviderId = :providerId
        AND a.enabled = true AND a.locked = false AND a.deleted = false
    """)
    Optional<Account> findActiveByOauth2ProviderAndOauth2ProviderId(String provider, String providerId);

    /**
     * Updates the failed login attempts count for a user.
     * 
     * Used for tracking consecutive failed login attempts for security purposes.
     * 
     * @param email The email of the user to update
     * @param attempts The new failed attempts count
     */
    /* 
    @Modifying
    @Query("""
        UPDATE Account a
        SET a.failedLoginAttempts = :attempts
        WHERE a.email = :email
    """)
    void updateFailedLoginAttempts(@Param("email") String email, @Param("attempts") Integer attempts);
    */
    /**
     * Locks a user account until the specified time.
     * 
     * Sets account as locked and defines when the lock expires.
     * Used for temporary account suspension due to security violations.
     * 
     * @param email The email of the user to lock
     * @param lockedUntil The timestamp when the lock expires
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
     * Removes account lock, clears lock expiration time, and resets
     * failed login attempts counter to zero.
     * 
     * @param email The email of the user to unlock
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
