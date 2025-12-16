package com.suyos.sessionservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.suyos.sessionservice.model.Session;

/**
 * Repository interface for Session entity data access operations.
 * 
 * <p>Handles session storage, validation, and cleanup operations for user
 * session management.</p>
 * 
 * @author Joel Salazar
 */
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Finds a session for an account.
     * 
     * @param accountId Account ID linked to the session
     * @return Optional containing session if found, empty otherwise
     */
    Optional<Session> findByAccountId(UUID accountId);

    /**
     * Finds all sessions for an account.
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
     * Finds all active sessions for an account.
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

    /**
     * Terminates all active sessions for an account.
     * 
     * @param accountId Account linked to the session to terminate
     */
    @Modifying
    @Query("""
        UPDATE Session s 
        SET s.active = false, s.terminatedAt = CURRENT_TIMESTAMP
        WHERE s.accountId = :accountId
        AND s.active = true
    """)
    void terminateAllActiveByAccountId(UUID accountId);

    /**
     * Deletes all sessions for an account.
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
     * Deletes expired sessions.
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