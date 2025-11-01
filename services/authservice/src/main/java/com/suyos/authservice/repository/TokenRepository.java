package com.suyos.authservice.repository;

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
 * <p>Handles refresh token storage, validation, and cleanup operations
 * for JWT authentication flows.</p>
 *
 * @author Joel Salazar
 */
public interface TokenRepository extends JpaRepository<Token, UUID> {

    /**
     * Finds all valid refresh tokens for an account.
     * 
     * Returns non-expired and non-revoked tokens for token cleanup.
     * 
     * @param accountId Account ID to search tokens for
     * @return List of valid tokens for the account
     */
    @Query("""
        SELECT t FROM Token t 
        WHERE t.account.id = :accountId 
        AND t.revoked = false
    """)
    List<Token> findAllValidTokensByAccountId(UUID accountId);

    /**
     * Finds a refresh token by its value.
     * 
     * Used for validating refresh tokens during token refresh flow.
     * 
     * @param refreshToken Token value to search for
     * @return Optional containing token if found, empty otherwise
     */
    Optional<Token> findByToken(String refreshToken);

    /**
     * Revokes all active tokens for an account.
     * 
     * Marks all non-revoked tokens as revoked for security purposes.
     * 
     * @param accountId Account ID to revoke tokens for
     */
    @Modifying
    @Query("""
        UPDATE Token t 
        SET t.revoked = true
        WHERE t.account.id = :accountId AND t.revoked = false
    """)
    void revokeAllTokensByAccountId(UUID accountId);

    /**
     * Deletes all refresh tokens for an account.
     * 
     * Used during account deletion for cleanup.
     * 
     * @param accountId Account ID to delete tokens for
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.account.id = :accountId")
    void deleteAllByAccountId(UUID accountId);
    
}