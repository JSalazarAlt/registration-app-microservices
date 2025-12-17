package com.suyos.sessionservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.common.event.GlobalSessionTerminationEvent;
import com.suyos.common.event.SessionCreationEvent;
import com.suyos.common.event.SessionTerminationEvent;
import com.suyos.common.model.SessionTerminationReason;
import com.suyos.sessionservice.dto.SessionInfoDTO;
import com.suyos.sessionservice.exception.exceptions.SessionNotFoundException;
import com.suyos.sessionservice.mapper.SessionMapper;
import com.suyos.sessionservice.model.ProcessedEvent;
import com.suyos.sessionservice.model.Session;
import com.suyos.sessionservice.repository.ProcessedEventRepository;
import com.suyos.sessionservice.repository.SessionRepository;

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

    /** Repository for processed event data access operations */
    private final ProcessedEventRepository processedEventRepository;

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
    public List<SessionInfoDTO> findAllSessionsByAccount(UUID accountId) {
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
    public SessionInfoDTO createSession(SessionCreationEvent event) {
        // Log session creation attempt
        log.info("event=session_creation_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            log.info("event=duplicate_event_ignored event_id={}", event.getId());
            return null;
        }

        // Create new processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist new processed event
        processedEventRepository.save(newEvent);

        // Map session from session's information
        Session session = sessionMapper.toEntity(event);

        // Set session expiration time
        session.setExpiresAt(Instant.now().plusSeconds(SESSION_LIFETIME_DAYS * 24 * 3600));
        
        // Persist created session
        Session createdSession = sessionRepository.save(session);

        // Log session creation success
        log.info("event=session_created account_id={}", createdSession.getAccountId());

        // Map session's information from created user
        SessionInfoDTO sessionInfo = sessionMapper.toSessionInfoDTO(createdSession);

        // Return created session's information
        return sessionInfo;
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
    public void terminateSessionById(UUID id) {
        // Log session termination attempt
        log.info("event=session_termination_attempt id={}", id);

        // Look up session by account ID
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new SessionNotFoundException("id=" + id));

        // Ensure sesion is active
        if (!session.getActive()) {
            return;
        }

        // Terminate session
        session.setActive(false);
        session.setTerminationReason(SessionTerminationReason.REVOKED);
        session.setTerminatedAt(Instant.now());

        // Persist terminated session
        Session terminatedSession = sessionRepository.save(session);

        // Log session termination success
        log.info("event=session_terminated id={} account_id={} termination_reason={}", 
            terminatedSession.getAccountId(), terminatedSession.getId(), SessionTerminationReason.REVOKED);
    }

    /**
     * Terminates a session by ID for a user request.
     * 
     * @param event Account ID associated with the session and termination reason
     */
    @Transactional
    public void terminateSession(SessionTerminationEvent event) {
        // Log session termination attempt
        log.info("event=session_termination_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            log.info("event=duplicate_event_ignored event_id={}", event.getId());
            return;
        }

        // Create new processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist new processed event
        processedEventRepository.save(newEvent);

        // Look up session by account ID
        Session session = sessionRepository.findById(event.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException("id=" + event.getSessionId()));

        // Ensure sesion is active
        if (!session.getActive()) {
            return;
        }

        // Terminate session
        session.setActive(false);
        session.setTerminationReason(event.getTerminationReason());
        session.setTerminatedAt(Instant.now());

        // Persist terminated session
        Session terminatedSession = sessionRepository.save(session);

        // Log session termination success
        log.info("event=session_terminated id={} account_id={} termination_reason={}", 
            terminatedSession.getAccountId(), terminatedSession.getId(), event.getTerminationReason());
    }

    /**
     * Terminates all sessions by account's ID for an admin request.
     *
     * @param accountId Account's ID to terminate all sessions
     */
    @Transactional
    public void terminateAllSessionsByAccountId(UUID accountId) {
        // Log session termination attempt
        log.info("event=global_session_termination_attempt account_id={}", accountId);

        // Terminate all active sessions by account ID
        sessionRepository.terminateAllActiveByAccountId(accountId, SessionTerminationReason.REVOKED);

        // Log global session termination success
        log.info("event=sessions_globally_terminated account_id={} termination_reason={}",
            accountId, SessionTerminationReason.REVOKED);
    }

    /**
     * Terminates all sessions by account's ID for a user request.
     *
     * @param accountId Account's ID to terminate all sessions
     */
    @Transactional
    public void terminateAllSessions(GlobalSessionTerminationEvent event) {
        // Log session termination attempt
        log.info("event=global_session_termination_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            log.info("event=duplicate_event_ignored event_id={}", event.getId());
            return;
        }

        // Create new processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist new processed event
        processedEventRepository.save(newEvent);

        // Terminate all active sessions by account ID
        sessionRepository.terminateAllActiveByAccountId(event.getAccountId(), event.getTerminationReason());

        // Log global session termination success
        log.info("event=sessions_globally_terminated account_id={} termination_reason={}",
            event.getAccountId(), event.getTerminationReason());
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