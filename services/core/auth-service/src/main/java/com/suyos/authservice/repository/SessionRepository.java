package com.suyos.authservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.authservice.model.Session;
import com.suyos.authservice.model.SessionTerminationReason;

/**
 * Repository for session data access operations.
 * 
 * <p>Provides standard CRUD, bulk termination, and cleanup operations for
 * session entities.</p>
 */
public interface SessionRepository extends JpaRepository<Session, UUID> {

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    List<Session> findAllByAccountId(UUID accountId);

    List<Session> findAllByAccountIdAndActiveTrue(UUID accountId);

    // ----------------------------------------------------------------
    // TERMINATION
    // ----------------------------------------------------------------

    @Modifying
    @Query("""
        UPDATE Session s 
        SET s.active = false,
            s.terminationReason = :terminationReason,
            s.terminatedAt = CURRENT_TIMESTAMP
        WHERE s.accountId = :accountId
        AND s.active = true
    """)
    void terminateAllActiveByAccountId(UUID accountId, SessionTerminationReason terminationReason);

    // ----------------------------------------------------------------
    // DELETION
    // ----------------------------------------------------------------
    
    void deleteAllByAccountId(UUID accountId);

    /**
     * Deletes all expired sessions by their account ID.
     * 
     * @param cutoffDate Threshold timestamp
     */
    void deleteAllByActiveFalseAndTerminatedAtBefore(Instant cutoffDate);
    
}