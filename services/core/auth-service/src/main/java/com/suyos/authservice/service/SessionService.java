package com.suyos.authservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.internal.SessionCreationRequest;
import com.suyos.authservice.dto.response.SessionInfoResponse;
import com.suyos.authservice.exception.exceptions.SessionNotFoundException;
import com.suyos.authservice.mapper.SessionMapper;
import com.suyos.authservice.model.Session;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.model.TokenType;
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

    /** Service for account management */
    private final AccountService accountService;

    /** Service for token management */
    private final TokenService tokenService;

    /** Session lifetime in days */
    private static final Long SESSION_LIFETIME_DAYS = 30L;

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds a session by ID.
     *
     * @param id Session's ID to search for
     * @return Session's information
     */
    public SessionInfoResponse findSessionById(UUID id) {
        // Look up session by ID
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("session_id=" + id));

        // Log session found by ID success
        log.info("event=session_found_by_id session_id={}", session.getId());

        // Map session's information from session
        SessionInfoResponse sessionInfo = sessionMapper.toSessionInfoDTO(session);

        // Return session's information
        return sessionInfo;
    }

    /**
     * Finds all active sessions by account ID.
     *
     * @param accountId Account's ID to search sessions for
     * @return List of active sessions' information
     */
    public List<SessionInfoResponse> findAllSessionsByAccountId(UUID accountId) {
        // Find all active sessions by account ID and map them to sessions' information
        List<SessionInfoResponse> sessions = sessionRepository.findAllActiveByAccountId(accountId)
            .stream()
            .map(sessionMapper::toSessionInfoDTO)
            .toList();

        // Log all active sessions found by account ID success
        log.info("event=all_sessions_found_by_account_id account_id={}", accountId);

        // Return list of active sessions' information
        return sessions;
    }

    // ----------------------------------------------------------------
    // CREATION
    // ----------------------------------------------------------------

    /**
     * Creates a new active session.
     *
     * @param request Account's ID associated with the session and session's information
     * @return Created session's information
     */
    public Session createSession(SessionCreationRequest request) {
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
     *  Terminates an active session by ID.
     * 
     * @param id Session's ID to terminate
     * @param terminationReason Termination reason
     */
    public void terminateSessionById(UUID id, SessionTerminationReason terminationReason) {
        // Log session termination attempt
        log.info("event=session_termination_attempt session_id={}", id);

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
                terminatedSession.getId(), terminatedSession.getAccountId(), terminatedSession.getTerminationReason());
    }

    /**
     * Terminates all active sessions by account's ID.
     *
     * @param accountId Account's ID to terminate all sessions
     * @param terminationReason Termination reason
     */
    public void terminateAllSessionsByAccountId(UUID accountId, SessionTerminationReason terminationReason) {
        // Log all sessions termination attempt
        log.info("event=all_sessions_termination_attempt account_id={}", accountId);

        // Update account's last logout timestamp
        accountService.updateLastLogout(accountId);

        // Revoke all active refresh tokens by account ID
        tokenService.revokeAllTokensByAccountIdAndType(accountId, TokenType.REFRESH);

        // Terminate all active sessions by account ID
        sessionRepository.terminateAllActiveByAccountId(accountId, terminationReason);

        // Log all sessions termination success
        log.info("event=all_sessions_terminated account_id={} termination_reason={}",
                accountId, terminationReason);
    }

    // ----------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------

    /**
     * Updates the last known IP for a session.
     *
     * @param sessionId session identifier
     * @param ipAddress new IP address
     */
    @Transactional
    public void updateLastActivity(UUID sessionId, String ipAddress) {
        sessionRepository.findById(sessionId).ifPresent(session ->{
            session.setLastIpAddress(ipAddress);
            session.setLastAccessedAt(Instant.now());
        });
    }

}