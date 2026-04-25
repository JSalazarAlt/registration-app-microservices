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

public interface TokenRepository extends JpaRepository<Token, UUID> {

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    Optional<Token> findByValue(String value);

    Optional<Token> findByValueAndType(String value, TokenType type);

    List<Token> findAllByAccount_Id(UUID accountId);

    // ----------------------------------------------------------------
    // REVOCATION
    // ----------------------------------------------------------------

    /**
     * Revokes all valid tokens by their account ID.
     * 
     * <p>Used during account deletion and password changes.</p>
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
    void revokeAllValidByAccount_Id(UUID accountId);

    /**
     * Revokes all valid tokens by their account ID and type.
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
    void revokeAllValidByAccount_IdAndType(UUID accountId, TokenType type);

    // ----------------------------------------------------------------
    // DELETION
    // ----------------------------------------------------------------
    
    void deleteAllByAccount_Id(UUID accountId);

    // ----------------------------------------------------------------
    // CLEAN-UP
    // ----------------------------------------------------------------

    void deleteAllByRevokedTrueAndExpiresAtBefore(Instant cutoffDate);
    
}