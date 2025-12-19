package com.suyos.authservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.internal.SessionCreationRequestDTO;
import com.suyos.authservice.dto.response.SessionInfoDTO;
import com.suyos.authservice.exception.exceptions.SessionNotFoundException;
import com.suyos.authservice.mapper.SessionMapper;
import com.suyos.authservice.model.Session;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.repository.SessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for session management operations.
 *
 * <p>Handles session retrieval, creation, update, and termination.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SessionService {

    /** Repository for session data access operations */
    private final SessionRepository sessionRepository;

    /** Mapper for converting between session entities and DTOs */
    private final SessionMapper sessionMapper;

    /** Session lifetime in days */
    private static final Long SESSION_LIFETIME_DAYS = 30L;

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Retrieves all sessions for an account.
     *
     * @param accountId Account's ID to search sessions for
     * @return List of active sessions
     */
    public List<SessionInfoDTO> findAllSessionsByAccountId(UUID accountId) {
        // Find all active sessions by account ID
        List<SessionInfoDTO> sessions = sessionRepository.findAllActiveByAccountId(accountId)
            .stream()
            .map(sessionMapper::toSessionInfoDTO)
            .toList();

        // Return list of active sessions
        return sessions;
    }

    // ----------------------------------------------------------------
    // CREATION
    // ----------------------------------------------------------------

    /**
     * Creates a new active session.
     *
     * @param event Account's ID associated with the session and session's information
     * @return Created session's information
     */
    public Session createSession(SessionCreationRequestDTO request) {
        // Log session creation attempt
        log.info("event=session_creation_attempt account_id={}", request.getAccountId());

        // Map session from session's information
        Session session = sessionMapper.toEntity(request);

        // Set session expiration time
        session.setExpiresAt(Instant.now().plusSeconds(SESSION_LIFETIME_DAYS * 24 * 3600));
        
        // Persist created session
        Session createdSession = sessionRepository.save(session);

        // Log session creation success
        log.info("event=session_created account_id={}", createdSession.getAccountId());

        // Return created session's information
        return createdSession;
    }

    // ----------------------------------------------------------------
    // TERMINATION
    // ----------------------------------------------------------------
    
    /**
     * Terminates a session by ID for a user request.
     * 
     * @param event Account ID associated with the session and termination reason
     */
    @Transactional
    public void terminateSessionById(UUID id, UUID accountId, SessionTerminationReason terminationReason) {
        // Log session termination attempt
        log.info("event=session_termination_attempt account_id={}", accountId);

        // Look up session by account ID
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("id=" + id));

        // Ensure sesion is active
        if (!session.getActive()) {
            return;
        }

        // Terminate session
        session.setActive(false);
        session.setTerminationReason(terminationReason);
        session.setTerminatedAt(Instant.now());

        // Persist terminated session
        Session terminatedSession = sessionRepository.save(session);

        // Log session termination success
        log.info("event=session_terminated id={} account_id={} termination_reason={}", 
            terminatedSession.getAccountId(), terminatedSession.getId(), terminatedSession.getTerminationReason());
    }

    /**
     * Terminates all sessions by account's ID for an admin request.
     *
     * @param accountId Account's ID to terminate all sessions
     */
    @Transactional
    public void terminateAllSessionsByAccountId(UUID accountId, SessionTerminationReason terminationReason) {
        // Log session termination attempt
        log.info("event=global_session_termination_attempt account_id={}", accountId);

        // Terminate all active sessions by account ID
        sessionRepository.terminateAllActiveByAccountId(accountId, terminationReason);

        // Log global session termination success
        log.info("event=sessions_globally_terminated account_id={} termination_reason={}",
            accountId, terminationReason);
    }

    // ----------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------

    /**
     * Updates the last known IP for a session.
     *
     * @param sessionId session identifier
     * @param ipAddress new IP address
     */
    @Transactional
    public void updateLastIpAddress(UUID sessionId, String ipAddress) {
        sessionRepository.findById(sessionId).ifPresent(session ->
                session.setLastIpAddress(ipAddress)
        );
    }

}