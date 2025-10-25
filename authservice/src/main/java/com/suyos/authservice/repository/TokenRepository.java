package com.suyos.authservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.authservice.model.Token;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    /**
     * Finds all valid (non-expired and non-revoked) refresh tokens for the given 
     * account. Used to revoke existing tokens when issuing a new one.
     */
    @Query("""
        SELECT t FROM Token t 
        WHERE t.account.id = :accountId 
        AND t.revoked = false
    """)
    List<Token> findAllValidTokensByAccountId(UUID accountId);

    /**
     * Finds a refresh token by its raw value (for validating refresh tokens).
     */
    Optional<Token> findByToken(String refreshToken);

    /**
     * Revokes all active tokens for a specific account.
     */
    @Modifying
    @Query("""
        UPDATE Token t 
        SET t.revoked = true
        WHERE t.account.id = :accountId AND t.revoked = false
    """)
    void revokeAllTokensByAccountId(UUID accountId);

    /**
     * Deletes all refresh tokens for a specific account (for account deletion).
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.account.id = :accountId")
    void deleteAllByAccountId(UUID accountId);
    
}