package com.suyos.authservice.repository;

import java.time.LocalDateTime;
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
 *
 * @author Joel Salazar
 */
public interface TokenRepository extends JpaRepository<Token, UUID> {

    // TOKEN LOOKUP

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
     * Finds all tokens for a specified account ID.
     * 
     * @param accountId Account ID to search tokens for
     * @return List of tokens associated with the account
     */
    List<Token> findAllByAccountId(UUID accountId);

    // TOKEN REVOCATION

    /**
     * Revokes all valid tokens for an account.
     * 
     * <p>Used during .</p>
     * 
     * @param accountId Account ID to delete tokens for
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
     * Deletes all tokens of a specific type for an account.
     * 
     * @param accountId Account ID to delete tokens for
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

    // TOKEN DELETION

    /**
     * Deletes all tokens for an account.
     * 
     * @param accountId Account ID to delete tokens for
     */
    @Modifying
    @Query("""
        DELETE FROM Token t 
        WHERE t.account.id = :accountId
    """)
    void deleteAllByAccountId(UUID accountId);

    // TOKEN VALIDATION

    /**
     * Finds all valid tokens by value (not revoked, not expired).
     * 
     * @param value Token value to search for
     * @return Optional containing valid token if found, empty otherwise
     */
    @Query("""
        SELECT t FROM Token t 
        WHERE t.value = :value 
        AND t.revoked = false 
        AND t.expiresAt > CURRENT_TIMESTAMP
    """)
    Optional<Token> findValidByValue(String value);


    // TOKEN CLEAN-UP

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
    void deleteExpiredOrRevoked(LocalDateTime now);
    
}