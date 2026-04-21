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
 * Repository interface for Session entity data access operations.
 * 
 * <p>Provides standard CRUD operations for session entities. Handles session
 * termination and cleanup operations.</p>
 */
public interface SessionRepository extends JpaRepository<Session, UUID> {

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    /**
     * Finds all sessions by account ID.
     * 
     * @param accountId Account ID to search for
     * @return List of sessions associated with the account
     */
    List<Session> findAllByAccountId(UUID accountId);

    /**
     * Finds all active sessions by account ID.
     * 
     * @param accountId Account ID to search for
     * @return List of active sessions associated with the account
     */
    List<Session> findAllByAccountIdAndActiveTrue(UUID accountId);

    // ----------------------------------------------------------------
    // TERMINATION
    // ----------------------------------------------------------------

    /**
     * Terminates all active sessions by account's ID.
     * 
     * @param accountId Account linked to the session to terminate
     */
    @Modifying
    @Query("""
        UPDATE Session s 
        SET s.active = false, s.terminationReason = :terminationReason, s.terminatedAt = CURRENT_TIMESTAMP
        WHERE s.accountId = :accountId
        AND s.active = true
    """)
    void terminateAllActiveByAccountId(UUID accountId, SessionTerminationReason terminationReason);

    // ----------------------------------------------------------------
    // DELETION
    // ----------------------------------------------------------------
    
    /**
     * Deletes all sessions for an account by account's ID.
     * 
     * @param accountId Account ID to delete sessions for
     */
    void deleteAllByAccountId(UUID accountId);

    /**
     * Deletes all expired sessions by account's ID.
     * 
     * @param now Current timestamp
     */
    void deleteAllByActiveFalseAndTerminatedAtBefore(Instant cutoffDate);
    
}