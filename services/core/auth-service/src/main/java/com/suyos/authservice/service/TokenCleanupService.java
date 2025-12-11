package com.suyos.authservice.service;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.repository.TokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for token cleanup operations.
 *
 * <p>Handles cleanup tasks such as removing expired or revoked tokens.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenCleanupService {

    /** Repository for token operations */
    private final TokenRepository tokenRepository;

    /**
     * Deletes expired and revoked tokens daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredAndRevokedTokens() {
        // Log cleanup start
        log.info("event=token_cleanup_started");

        // Delete expired or revoked tokens
        tokenRepository.deleteExpiredOrRevoked(Instant.now());

        // Log cleanup completion
        log.info("event=token_cleanup_completed");
    }

}