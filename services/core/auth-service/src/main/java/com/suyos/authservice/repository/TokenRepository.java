package com.suyos.authservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;

/**
 * Repository for token data access operations.
 *
 * <p>Provides standard CRUD operations for account entities. Handles token
 * lookup, revocation, deletion, and cleanup operations.</p>
 */
public interface TokenRepository extends JpaRepository<Token, UUID> {

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds a token by value.
     * 
     * @param value Token value to search for
     * @return Optional containing token if found, empty otherwise
     */
    Optional<Token> findByValue(String value);

    /**
     * Finds a token by value and type.
     *
     * @param value Token value to search for
     * @param type Token type to search for
     * @return Optional containing token if found, empty otherwise
     */
    Optional<Token> findByValueAndType(String value, TokenType type);

    /**
     * Finds all tokens by account ID.
     * 
     * @param accountId Account ID to search tokens for
     * @return List of tokens associated with the account
     */
    List<Token> findAllByAccountId(UUID accountId);

    // ----------------------------------------------------------------
    // REVOCATION
    // ----------------------------------------------------------------

    /**
     * Revokes all valid tokens by account ID.
     * 
     * <p>Used during account deletion and password changes.</p>
     * 
     * @param accountId Account ID to revoke valid tokens
     */
    @Modifying
    @Query("""
        UPDATE Token t
        SET t.revoked = true,
            t.revokedAt = CURRENT_TIMESTAMP
        WHERE t.account.id = :accountId
        AND t.revoked = false
        AND t.expiresAt > CURRENT_TIMESTAMP
    """)
    void revokeAllValidByAccountId(UUID accountId);

    /**
     * Revokes all valid tokens by account ID and type.
     * 
     * @param accountId Account ID to revoke valid tokens
     * @param type Token type to revoke
     */
    @Modifying
    @Query("""
        UPDATE Token t
        SET t.revoked = true,
            t.revokedAt = CURRENT_TIMESTAMP
        WHERE t.account.id = :accountId
        AND t.type = :type
        AND t.revoked = false
        AND t.expiresAt > CURRENT_TIMESTAMP
    """)
    void revokeAllValidByAccountIdAndType(UUID accountId, TokenType type);

    // ----------------------------------------------------------------
    // DELETION
    // ----------------------------------------------------------------

    /**
     * Deletes all tokens for an account.
     * 
     * @param accountId Account ID to delete tokens
     */
    @Modifying
    @Query("""
        DELETE FROM Token t 
        WHERE t.account.id = :accountId
    """)
    void deleteAllByAccountId(UUID accountId);

    // ----------------------------------------------------------------
    // CLEAN-UP
    // ----------------------------------------------------------------

    /**
     * Deletes all expired or revoked tokens.
     * 
     * @param now Current timestamp
     */
    @Modifying
    @Query("""
        DELETE FROM Token t
        WHERE t.revoked = true
        OR t.expiresAt < :now
    """)
    void deleteExpiredOrRevoked(Instant now);
    
}