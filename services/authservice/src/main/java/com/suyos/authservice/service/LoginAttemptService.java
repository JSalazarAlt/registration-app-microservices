package com.suyos.authservice.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.model.Account;
import com.suyos.authservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for handling login attempt tracking and account security.
 * 
 * <p>Manages failed login attempts and implements account locking mechanisms.
 * Uses separate transactions to ensure failed attempts are recorded even when
 * authentication fails.</p>
 * 
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    /** Repository for account data access operations */
    private final AccountRepository accountRepository;

    /** Maximum allowed failed login attempts before account lock */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    /** Account lock duration in hours */
    private static final int LOCK_DURATION_HOURS = 24;

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

        // Lock account if maximum attempts exceeded
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            account.setLocked(true);
            account.setLockedUntil(LocalDateTime.now().plusHours(LOCK_DURATION_HOURS));
        }

        // Persist updated account
        accountRepository.save(account);
    }

}