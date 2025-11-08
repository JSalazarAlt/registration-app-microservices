package com.suyos.authservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.authservice.model.Token;

/**
 * Repository interface for Token entity data access operations.
 *
 * <p>Handles refresh token storage, validation, and cleanup operations for
 * JWT authentication flows.</p>
 *
 * @author Joel Salazar
 */
public interface TokenRepository extends JpaRepository<Token, UUID> {

    /**
     * Finds a token by its value.
     * 
     * @param value Token value to search for
     * @return Optional containing token if found, empty otherwise
     */
    Optional<Token> findByValue(String value);

    /**
     * Finds a valid token by its value.
     * 
     * @param value Token value to search for
     * @return Optional containing token if found, empty otherwise
     */
    @Query("""
        SELECT t FROM Token t 
        WHERE t.value = :value 
        AND t.revoked = false AND t.expiresAt > CURRENT_TIMESTAMP
    """)
    Optional<Token> findValidByValue(String value);

    /**
     * Finds all valid (non-revoked, non-expired) tokens for an account.
     * 
     * @param accountId Account ID to search tokens for
     * @return List of valid tokens for the account
     */
    @Query("""
        SELECT t FROM Token t 
        WHERE t.account.id = :accountId
        AND t.revoked = false AND t.expiresAt > CURRENT_TIMESTAMP
    """)
    List<Token> findAllValidByAccountId(UUID accountId);

    /**
     * Revokes all valid tokens for an account (e.g., password change).
     * 
     * @param accountId Account ID to revoke tokens for
     */
    @Modifying
    @Query("""
        UPDATE Token t 
        SET t.revoked = true
        WHERE t.account.id = :accountId 
        AND t.revoked = false AND t.expiresAt > CURRENT_TIMESTAMP
    """)
    void revokeAllByAccountId(UUID accountId);

    /**
     * Deletes all tokens for an account (used during account deletion).
     * 
     * @param accountId Account ID to delete tokens for
     */
    @Modifying
    @Query("""
        DELETE FROM Token t 
        WHERE t.account.id = :accountId
    """)
    void deleteAllByAccountId(UUID accountId);

    /**
     * Deletes all expired or revoked tokens.
     * 
     * @param now Current timestamp
     */
    @Modifying
    @Query("""
        DELETE FROM Token t
        WHERE t.expiresAt < :now OR t.revoked = true
    """)
    void deleteExpiredOrRevoked(LocalDateTime now);
    
}