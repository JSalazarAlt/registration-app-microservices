package com.suyos.authservice.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.client.UserClient;
import com.suyos.authservice.dto.internal.UserCreationRequestDTO;
import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.dto.request.EmailResendRequestDTO;
import com.suyos.authservice.dto.request.EmailVerificationRequestDTO;
import com.suyos.authservice.dto.request.OAuthAuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.dto.response.EmailResendResponseDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Role;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for authentication operations.
 *
 * <p>Handles account creation, authentication, OAuth2 integration, and
 * interservice communication with User Service. Manages token generation
 * and account security features.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    /** Mapper for converting between account entities and DTOs */
    private final AccountMapper accountMapper;
    
    /** Repository for account data access operations */
    private final AccountRepository accountRepository;

    /** Service for token management */
    private final TokenService tokenService;

    /** Service for handling failed login attempts and account locking */
    private final LoginAttemptService loginAttemptService;
    
    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /** Web client for user-related interservice calls */
    private final UserClient userClient;

    // TRADITIONAL REGISTRATION AND LOGIN

    /**
     * Creates a new account with the provided registration data.
     * 
     * @param request Registration data
     * @return Created account's information
     * @throws RuntimeException If email or username already exists
     */
    public AccountInfoDTO createAccount(RegistrationRequestDTO request) {
        // Check if email is already registered
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if username is already taken
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already registered");
        }

        // Map account from account's registration DTO
        Account account = accountMapper.toEntity(request);

        // Set password and default role
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(Role.USER);
        
        // Persist created account
        Account createdAccount = accountRepository.save(account);

        // Create request to create a new user
        UserCreationRequestDTO userReq = new UserCreationRequestDTO();
        userReq.setAccountId(createdAccount.getId());
        userReq.setUsername(createdAccount.getUsername());
        userReq.setEmail(createdAccount.getEmail());
        userReq.setFirstName(request.getFirstName());
        userReq.setLastName(request.getLastName());
        userReq.setPhone(request.getPhone());
        userReq.setProfilePictureUrl(request.getProfilePictureUrl());
        userReq.setLocale(request.getLocale());
        userReq.setTimezone(request.getTimezone());

        // Call User microservice passing the request to create a new user
        userClient.createUser(userReq).block();

        // Issue email verification token
        tokenService.issueEmailVerificationToken(createdAccount);

        // Call Email microservice to send verification link
        //emailClient.sendEmail(emailReq).block();

        // Map account's information from created account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(createdAccount);
    
        // Return created account's information
        return accountInfo;
    }

    /**
     * Authenticates account credentials and generates tokens.
     * 
     * @param request Login credentials
     * @return Refresh and access tokens
     * @throws RuntimeException If authentication fails
     */
    public AuthenticationResponseDTO authenticateAccount(AuthenticationRequestDTO request) {
        // Find if there is an active account for the email
        Account account = accountRepository.findActiveByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + request.getEmail()));
        
        // Check if account is not deleted
        if (!account.getEmailVerified()) {
            throw new RuntimeException("Email not verified. Please check your inbox for the verification email");
        }

        // Check if account is not deleted
        if (account.getDeleted()) {
            throw new RuntimeException("Account deleted. Please reactivate your account if still possible");
        }

        // Check account's lock status
        if (account.getLocked()) {
            if (account.getLockedUntil() != null && account.getLockedUntil().isBefore(LocalDateTime.now())) {
                // Auto-unlock expired locks
                account.setLocked(false);
                account.setLockedUntil(null);
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
            } else {
                Duration remaining = Duration.between(LocalDateTime.now(), account.getLockedUntil());
                throw new RuntimeException(String.format("Account locked. Try again in %d minutes", remaining.toMinutes()));
            }
        }
        
        // Verify password match
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            loginAttemptService.recordFailedAttempt(account);
            throw new RuntimeException("Invalid email or password");
        }
        
        // Reset security fields on successful login
        account.setFailedLoginAttempts(0);
        account.setLastLoginAt(LocalDateTime.now());
        account.setLocked(false);
        account.setLockedUntil(null);
        
        // Persist updated account
        Account updatedAccount = accountRepository.save(account);
        
        // Issue refresh and access tokens for successful login
        AuthenticationResponseDTO response = tokenService.issueRefreshAndAccessTokens(updatedAccount);

        // Return refresh and access tokens
        return response;
    }

    // OAUTH2 REGISTRATION AND LOGIN

    /**
     * Creates a new account from Google OAuth2 authentication.
     * 
     * @param request Registration data from Google
     * @return Created account entity
     */
    private Account createGoogleOAuth2Account(OAuthAuthenticationRequestDTO request) {
        // Create a new account with Google OAuth2 details
        Account account = Account.builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER)
                .oauth2Provider("google")
                .oauth2ProviderId(request.getProviderId())
                .emailVerified(true)
                .enabled(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();

        // Persist the created account
        Account createdAccount = accountRepository.save(account);
        
        // Return the created account
        return createdAccount;
    }

    /**
     * Processes Google OAuth2 authentication (create or link account).
     *
     * <p>Creates new account if not exists, or links existing account with
     * Google OAuth2. Generates JWT tokens for API access.</p>
     *
     * @param request Login credentials from Google
     * @return Refresh and access tokens
     */
    public AuthenticationResponseDTO processGoogleOAuth2Account(OAuthAuthenticationRequestDTO request) {
        // Find if there is an active account for the OAuth2 credentials or email
        Account account = accountRepository.findActiveByOauth2ProviderAndOauth2ProviderId("google", request.getProviderId())
            .or(() -> accountRepository.findByEmail(request.getEmail())
                .map(existingAccount -> {
                    // Link existing account with Google
                    existingAccount.setOauth2Provider("google");
                    existingAccount.setOauth2ProviderId(request.getProviderId());
                    existingAccount.setEmailVerified(true);
                    return accountRepository.save(existingAccount);
                })
            )
            // Create new account if not found
            .orElseGet(() -> createGoogleOAuth2Account(request));

        // Update last login time
        account.setLastLoginAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        // Issue refresh and access tokens for successful Google OAuth2 login
        AuthenticationResponseDTO response = tokenService.issueRefreshAndAccessTokens(savedAccount);

        // Return refresh and access tokens
        return response;
    }

    // LOGOUT

    /**
     * Deauthenticates account and revokes refresh token.
     * 
     * @param request Refresh token value
     * @throws RuntimeException If token is invalid
     */
    public void deauthenticateAccount(RefreshTokenRequestDTO request) {
        // Extract refresh token value from request
        String value = request.getValue();

        // Find if there is an account for the refresh token
        Account account = tokenService.findAccountByToken(value);

        // Update last logout timestamp
        account.setLastLogoutAt(LocalDateTime.now());

        // Persist updated account
        accountRepository.save(account);
        
        // Revoke the refresh token
        tokenService.revokeTokenByValue(value);
    }

    // EMAIL MANAGEMENT

    /**
     * Verifies email using the email verification token.
     *
     * @param request Email verification token value
     * @return Verified account's information
     * @throws RuntimeException If invalid token
     */
    public AccountInfoDTO verifyEmail(EmailVerificationRequestDTO request) {
        // Extract email verification token value from request
        String value = request.getValue();

        // Check if token is valid
        if(tokenService.isTokenValid(value) == false) {
            throw new RuntimeException("Invalid token");
        }

        // Find account associated with the token
        Account account = tokenService.findAccountByToken(value);

        // Set email as verified
        account.setEmailVerified(true);

        // Persist updated account
        accountRepository.save(account);

        // Delete the used email verification token for cleanup
        tokenService.deleteToken(value);

        // Map account's information from verified account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);

        // Return verified account's information
        return accountInfo;
    }

    /**
     * Resends email verification link.
     *
     * @param request Email
     * @return Message indicating if email verification link has been sent
     */
    public EmailResendResponseDTO resendEmailVerification(EmailResendRequestDTO request) {
        // Find if there is an account for the email
        accountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            if (!account.getEmailVerified()) {
                // Revoke old email verification tokens
                tokenService.revokeAllTokensByAccountIdAndType(account.getId(), TokenType.EMAIL_VERIFICATION);
                
                // Issue new token
                tokenService.issueEmailVerificationToken(account);
                
                // Call Email service to send verification link
                //
            }
        });

        // Build response
        EmailResendResponseDTO response = EmailResendResponseDTO.builder()
                .message("If your email is registered, a verification link has been sent.")
                .build();

        // Return message indicating if email verification link has been sent
        return response;
    }

}