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
 * <p>Manages failed login attempts, implements account locking mechanisms, and 
 * provides security features to prevent brute force attacks. Uses separate 
 * transactions to ensure failed attempts are recorded even when authentication 
 * fails.</p>
 * 
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    /** Repository for user data access operations */
    private final AccountRepository accountRepository;

    /** Maximum allowed failed login attempts before account lock */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    /** Account lock duration in hours */
    private static final int LOCK_DURATION_HOURS = 24;

    /**
     * Records a failed login attempt and implements account locking security.
     * 
     * <p>Increments the failed login attempt counter for the {@link Account} and 
     * automatically locks the account if the maximum number of failed attempts is 
     * reached. Uses REQUIRES_NEW transaction propagation to ensure failed attempts
     * are saved even if the calling transaction rolls back.</p>
     * 
     * @param user the user who had a failed login attempt
     * @throws IllegalArgumentException if user is null
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(Account account) {
        // Increase the failed login attemps counter by 1
        int attempts = account.getFailedLoginAttempts() + 1;
        account.setFailedLoginAttempts(attempts);

        // Lock the account if the number of attemps is greater than the maximum
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            account.setAccountLocked(true);
            account.setLockedUntil(LocalDateTime.now().plusHours(LOCK_DURATION_HOURS));
        }

        // Persist the updated account
        accountRepository.save(account);
    }

}