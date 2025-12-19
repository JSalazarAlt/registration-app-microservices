package com.suyos.authservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.model.Session;

/**
 * Repository interface for Session entity data access operations.
 * 
 * <p>Handles session storage, validation, and cleanup operations for user
 * session management.</p>
 * 
 * @author Joel Salazar
 */
public interface SessionRepository extends JpaRepository<Session, UUID> {

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds all sessions by account's ID.
     * 
     * @param accountId Account ID to search for
     * @return List of sessions associated with the account
     */
    @Query("""
        SELECT s FROM Session s 
        WHERE s.accountId = :accountId
    """)
    List<Session> findAllByAccountId(UUID accountId);

    /**
     * Finds all active sessions by account's ID.
     * 
     * @param accountId Account ID linked to active sessions
     * @return List of active sessions associated with the account
     */
    @Query("""
        SELECT s FROM Session s 
        WHERE s.accountId = :accountId
        AND s.active = true
    """)
    List<Session> findAllActiveByAccountId(UUID accountId);

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
        SET s.active = false, s.terminationReason =: reason, s.terminatedAt = CURRENT_TIMESTAMP
        WHERE s.accountId = :accountId
        AND s.active = true
    """)
    void terminateAllActiveByAccountId(UUID accountId, SessionTerminationReason reason);

    // ----------------------------------------------------------------
    // DELETION
    // ----------------------------------------------------------------
    
    /**
     * Deletes all sessions for an account by account's ID.
     * 
     * @param accountId Account ID to delete sessions for
     */
    @Modifying
    @Query("""
        DELETE FROM Session s 
        WHERE s.accountId = :accountId
    """)
    void deleteAllByAccountId(UUID accountId);

    /**
     * Deletes all expired sessions by account's ID.
     * 
     * @param now Current timestamp
     */
    @Modifying
    @Query("""
        DELETE FROM Session s 
        WHERE s.active = false
        AND s.terminatedAt <: cutoffDate  
    """)
    void deleteAllInactive(Instant cutoffDate);
    
}