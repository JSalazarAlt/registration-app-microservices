package com.suyos.authservice.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.authservice.model.Account;

/**
 * Repository for account data access operations.
 * 
 * <p>Provides standard CRUD and lock/unlock operations for account entities.</p>
 */
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {
    
    // ----------------------------------------------------------------
    // EXISTENCE
    // ----------------------------------------------------------------

    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    /**
     * Finds an account by OAuth2 provider (e.g., "google") and provider ID.
     */
    Optional<Account> findByOauth2ProviderAndOauth2ProviderId(String provider, String providerId);

    // ----------------------------------------------------------------
    // LOCK AND UNLOCK
    // ----------------------------------------------------------------

    @Modifying
    @Query("""
        UPDATE Account a
        SET a.locked = true, a.lockedUntil = :until 
        WHERE a.id = :id
    """)
    int lockAccountById(UUID id, Instant until);

    @Modifying
    @Query("""
        UPDATE Account a
        SET a.locked = false, a.lockedUntil = null 
        WHERE a.id = :id
    """)
    int unlockAccountById(UUID id);

}