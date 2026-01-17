package com.suyos.authservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.PasswordChangeRequest;
import com.suyos.authservice.dto.request.PasswordForgotRequest;
import com.suyos.authservice.dto.request.PasswordResetRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.dto.response.GenericMessageResponse;
import com.suyos.authservice.exception.exceptions.AccountNotFoundException;
import com.suyos.authservice.exception.exceptions.InvalidTokenException;
import com.suyos.authservice.exception.exceptions.PasswordMismatchException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for password-related operations.
 *
 * <p>Handles password reset processing and password change operations. Uses
 * {@link TokenService} for generating, validating, and revoking password reset
 * tokens.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordService {

    /** Mapper for converting between account entities and DTOs */
    private final AccountMapper accountMapper;
    
    /** Repository for account data access operations */
    private final AccountRepository accountRepository;

    /** Service for token management */
    private final TokenService tokenService;

    /** Service for session management */
    private final SessionService sessionService;

    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /** Password reset token lifetime in hours */
    private static final Long PASSWORD_TOKEN_LIFETIME_HOURS = 24L;

    // ----------------------------------------------------------------
    // PASSWORD RESET
    // ----------------------------------------------------------------

    /**
     * Initiates the password reset process.
     * 
     * <p>Generates and sends a password reset token to the account associated
     * with the provided email if account exists. Revokes any existing password
     * reset tokens before issuing a new one.</p>
     *
     * @param request Email address to send password reset link
     * @return Message indicating if password reset link has been sent
     */
    public GenericMessageResponse requestPasswordReset(PasswordForgotRequest request) {
        // Log password reset attempt
        log.info("event=password_reset_attempt email={}", request.getEmail());

        // Look up account by email
        accountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            if (!account.getEmailVerified()) {
                // Revoke old email verification tokens
                tokenService.revokeAllTokensByAccountIdAndType(account.getId(), TokenType.PASSWORD_RESET);
                
                // Issue new token
                tokenService.issueToken(account, TokenType.PASSWORD_RESET, PASSWORD_TOKEN_LIFETIME_HOURS);

                // Log password reset token issuance event
                log.info("event=password_reset_token_issued account_id={}", account.getId());
                
                // Publish event for Notification microservice to send password reset email
                //
            }
        });
        
        // Build response
        GenericMessageResponse response = GenericMessageResponse.builder()
                .message("A password reset link has been sent.")
                .build();

        // Return message indicating if password reset link has been sent
        return response;
    }

    /**
     * Resets an account's password.
     * 
     * <p>Resets an account's password if the token is valid. Revokes the
     * used password reset token.</p>
     *
     * @param request Password reset token value and new password
     * @return Updated account's information
     * @throws InvalidPasswordTokenException If password reset token is invalid
     */
    public AccountInfoResponse confirmPasswordReset(PasswordResetRequest request) {
        // Log password reset attempt
        log.info("event=password_reset_confirmation_attempt token={}", request.getValue());

        // Extract password reset token value from request
        String value = request.getValue();

        // Look up token by value and type
        Token passwordResetToken = tokenService.findTokenByValueAndType(value, TokenType.PASSWORD_RESET);

        // Ensure password reset token is valid
        if(!tokenService.isTokenValid(passwordResetToken)) {
            throw new InvalidTokenException(TokenType.PASSWORD_RESET);
        }

        // Get account linked to password reset token
        Account account = passwordResetToken.getAccount();

        // Reset password and update last password change timestamp
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setLastPasswordChangedAt(Instant.now());
        
        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Revoke password reset token used
        tokenService.revokeTokenByValue(value);

        // Log password reset event
        log.info("event=password_reset_confirmed account_id={}", updatedAccount.getId());

        // Map account's information from updated account
        AccountInfoResponse accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information
        return accountInfo;
    }

    // ----------------------------------------------------------------
    // PASSWORD CHANGE
    // ----------------------------------------------------------------

    /**
     * Changes an account's password.
     * 
     * <p>Changes an account's password if the current password matches the
     * one stored in the database.</p>
     *
     * @param id Account's ID
     * @param request Current password and new password
     * @return Updated account's information
     * @throws PasswordMismatchException If current password is invalid
     */
    public AccountInfoResponse changePassword(UUID id, PasswordChangeRequest request) {
        // Log password change attempt
        log.info("event=password_change_attempt account_id={}", id);

        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(),  account.getPassword())) {
            throw new PasswordMismatchException();
        }

        // Update password and last password change timestamp
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setLastPasswordChangedAt(Instant.now());
        
        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Terminate all active sessions (include revoke all active refresh tokens)
        sessionService.terminateAllSessionsByAccountId(updatedAccount.getId(), SessionTerminationReason.PASSWORD_CHANGED);

        // Log password change event
        log.info("event=password_changed account_id={}", updatedAccount.getId());

        // Map account's information from updated account
        AccountInfoResponse accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information
        return accountInfo;
    }
    
}