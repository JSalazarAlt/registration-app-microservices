package com.suyos.sessionservice.service;

import com.suyos.sessionservice.repository.SessionRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for session cleanup operations.
 *
 * <p>Handles cleanup tasks such as removing inactive sessions.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SessionCleanupService {
    
    /** Repository for token operations */
    private final SessionRepository sessionRepository;

    /** Number of days an inactive session remains persisted */
    private static final int CUTOFF_DAYS = 180;

    /**
     * Deletes expired and revoked tokens daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupInactiveSessions() {
        // Log cleanup start
        log.info("event=session_cleanup_started");

        // Calculate cutoff date (180 days ago)
        Instant cutoffDate = Instant.now().minus(CUTOFF_DAYS, ChronoUnit.DAYS);

        // Delete inactive sessions
        sessionRepository.deleteAllInactive(cutoffDate);

        // Log cleanup completion
        log.info("event=session_cleanup_completed");
    }

}