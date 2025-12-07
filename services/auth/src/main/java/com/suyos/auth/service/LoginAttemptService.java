package com.suyos.auth.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.auth.model.Account;
import com.suyos.auth.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling login attempt tracking and account security.
 * 
 * <p>Manages failed login attempts and implements account locking mechanisms.
 * Uses separate transactions to ensure failed attempts are recorded even when
 * authentication fails.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {
    
    /** Repository for account data access operations */
    private final AccountRepository accountRepository;

    /** Maximum allowed failed login attempts before account lock */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    /** Account lock duration in hours */
    private static final int LOCK_DURATION_HOURS = 2;

    /**
     * Records a failed login attempt and locks account if threshold reached.
     * 
     * @param account Account that had failed login attempt
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(Account account) {
        // Increment failed login attempts counter
        int attempts = account.getFailedLoginAttempts() + 1;
        account.setFailedLoginAttempts(attempts);

        // Log login failed event
        log.warn("event=login_failed attempts={} account_id={}", attempts, account.getId());

        // Lock account if maximum attempts exceeded and log account lock event
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            account.setLocked(true);
            account.setLockedUntil(Instant.now().plusSeconds(LOCK_DURATION_HOURS * 3600));
            log.warn("event=account_locked account_id={}", account.getId());
        }

        // Persist updated account
        accountRepository.save(account);
    }

}