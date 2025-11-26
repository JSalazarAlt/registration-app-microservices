package com.suyos.authservice.service;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for token cleanup operations.
 *
 * <p>Handles cleanup tasks such as removing expired or revoked tokens.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenCleanupService {

    /** Repository for token operations */
    private final TokenRepository tokenRepository;

    /**
     * Deletes expired and revoked tokens daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredAndRevokedTokens() {
        tokenRepository.deleteExpiredOrRevoked(Instant.now());
    }

}