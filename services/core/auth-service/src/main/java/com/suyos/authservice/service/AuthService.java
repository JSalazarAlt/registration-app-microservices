package com.suyos.authservice.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.internal.AuthenticationTokens;
import com.suyos.authservice.dto.internal.SessionCreationRequest;
import com.suyos.authservice.dto.request.AuthenticationRequest;
import com.suyos.authservice.dto.request.RegistrationRequest;
import com.suyos.authservice.dto.request.EmailResendRequest;
import com.suyos.authservice.dto.request.EmailVerificationRequest;
import com.suyos.authservice.dto.request.OAuth2AuthenticationRequest;
import com.suyos.authservice.dto.request.RefreshTokenRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.dto.response.GenericMessageResponse;
import com.suyos.authservice.event.AccountEventProducer;
import com.suyos.authservice.exception.exceptions.AccountDeletedException;
import com.suyos.authservice.exception.exceptions.AccountDisabledException;
import com.suyos.authservice.exception.exceptions.AccountLockedException;
import com.suyos.authservice.exception.exceptions.DuplicateRequestException;
import com.suyos.authservice.exception.exceptions.EmailNotVerifiedException;
import com.suyos.authservice.exception.exceptions.EmailAlreadyRegisteredException;
import com.suyos.authservice.exception.exceptions.EmailAlreadyVerifiedException;
import com.suyos.authservice.exception.exceptions.InvalidCredentialsException;
import com.suyos.authservice.exception.exceptions.InvalidPasswordException;
import com.suyos.authservice.exception.exceptions.InvalidRefreshTokenException;
import com.suyos.authservice.exception.exceptions.InvalidTokenException;
import com.suyos.authservice.exception.exceptions.UsernameAlreadyTakenException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.AccountRole;
import com.suyos.authservice.model.Session;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;
import com.suyos.authservice.util.ClientIpResolver;
import com.suyos.common.event.UserCreationEvent;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for authentication-related operations.
 *
 * <p>Handles account creation, authentication, OAuth2 and MFA integration,
 * and email verification. Communicates with the User, Email, and Session
 * microservices. Uses {@link TokenService} for token generation and 
 * revocation for authentication and email verification.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    /** Mapper for converting between account entities and DTOs */
    private final AccountMapper accountMapper;
    
    /** Repository for account data access operations */
    private final AccountRepository accountRepository;

    /** Kafka producer for account events */
    private final AccountEventProducer accountEventProducer;

    /** Service for token management */
    private final TokenService tokenService;

    /** Service for session management */
    private final SessionService sessionService;

    /** Service for handling failed login attempts and account locking */
    private final LoginAttemptService loginAttemptService;

    /** Service for handling duplicate requests using idempotency keys */
    private final IdempotencyService idempotencyService;

    /** Service for finding location of connection */
    private final GeoLocationService geoLocationService;

    /** Utility for finding client IP address */
    private final ClientIpResolver clientIpResolver;
    
    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /** Email verification token lifetime in hours */
    private static final Long EMAIL_TOKEN_LIFETIME_HOURS = 24L;

    // ----------------------------------------------------------------
    // TRADITIONAL CREATION AND AUTHENTICATION
    // ----------------------------------------------------------------

    /**
     * Creates a new account with the registration request.
     * 
     * <p>Creates a new account if the username and email are not already in
     * use. Publishes an event, so the User microservice creates a user linked
     * to the account.</p>
     * 
     * @param request Account's information and user's profile
     * @return Created account's information
     * @throws UsernameAlreadyTakenException If username is already in use
     * @throws EmailAlreadyRegisteredException If email is already registered
     */
    public AccountInfoResponse createAccount(RegistrationRequest request, String idempotencyKey) {
        // Log account creation attempt
        log.info("event=account_creation_attempt email={}", request.getEmail());

        // Check idempotency in Redis
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            boolean allowed = idempotencyService.checkAndLock(idempotencyKey, Duration.ofMinutes(10));
            if (!allowed) {
                throw new DuplicateRequestException();
            }
        }

        // Check if username is already taken
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyTakenException(request.getUsername());
        }
        
        // Check if email is already registered
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }

        // Map account from registration data
        Account account = accountMapper.toEntity(request);

        // Set password and default role
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(AccountRole.USER);
        
        // Persist created account
        Account createdAccount = accountRepository.save(account);

        // Log account creation success
        log.info("event=account_created account_id={}", createdAccount.getId());

        // Issue email verification token
        tokenService.issueToken(createdAccount, TokenType.EMAIL_VERIFICATION, EMAIL_TOKEN_LIFETIME_HOURS);

        // Generate random UUID and timestamp for user creation event
        String eventId = UUID.randomUUID().toString();
        Instant eventTimestamp = Instant.now();

        // Build user creation event
        UserCreationEvent event = UserCreationEvent.builder()
                .id(eventId)
                .occurredAt(eventTimestamp)
                .accountId(account.getId())
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .profilePictureUrl(request.getProfilePictureUrl())
                .locale(request.getLocale())
                .timezone(request.getTimezone())
                .build();
        
        // Publish user creation event
        accountEventProducer.publishUserCreation(event);

        // Map account's information from created account
        AccountInfoResponse accountInfo = accountMapper.toAccountInfoDTO(createdAccount);

        // Mark idempotency key as complete in Redis
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.markComplete(idempotencyKey, accountInfo.toString(), Duration.ofMinutes(10));
        }
    
        // Return created account's information
        return accountInfo;
    }

    /**
     * Authenticates an account using traditional credentials.
     * 
     * <p>Verifies an account using login credentials if enabled, verified,
     * and not locked. Updates login tracking fields, creates a new session,
     * and issues refresh and access tokens on successful authentication.</p>
     * 
     * @param request Account's credentials
     * @return Refresh and access tokens
     * @throws InvalidCredentialsException If username or email does not match any account
     * @throws AccountDisabledException If account is disabled
     * @throws EmailNotVerifiedException If account's email is not verified
     * @throws AccountDeletedException If account is deleted
     * @throws AccountLockedException If account is currently locked
     * @throws InvalidPasswordException If provided password is incorrect
     */
    public AuthenticationTokens authenticateAccount(
        AuthenticationRequest request,
        HttpServletRequest httpRequest
    ) {
        // Log account authentication attempt
        log.info("event=account_authentication_attempt identifier={}", request.getIdentifier());

        // Look up account by username or email
        Account account = accountRepository.findByUsername(request.getIdentifier())
            .or(() -> accountRepository.findByEmail(request.getIdentifier()))
            .orElseThrow(() -> new InvalidCredentialsException());

        // Ensure account is enabled
        if (!account.getEnabled()) {
            throw new AccountDisabledException();
        }

        // Ensure account is verified
        if (!account.getEmailVerified()) {
            throw new EmailNotVerifiedException(account.getEmail());
        }

        // Ensure account is not locked
        if (account.getLocked()) {
            if (account.getLockedUntil() != null && account.getLockedUntil().isBefore(Instant.now())) {
                // Auto-unlock account for expired locks
                account.setLocked(false);
                account.setLockedUntil(null);
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
                log.info("event=account_auto_unlocked account_id={}", account.getId());
            } else {
                // Communicate remaining lock time
                Duration remainingLockTime = Duration.between(Instant.now(), account.getLockedUntil());
                throw new AccountLockedException(String.valueOf(remainingLockTime.toMinutes()));
            }
        }

        // Reactivate account if was soft deleted
        if (account.getDeleted()) {
            account.setDeleted(false);
        }
        
        // Verify password match
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            loginAttemptService.recordFailedAttempt(account);
            throw new InvalidCredentialsException();
        }
        
        // Update login tracking fields on successful login
        account.setFailedLoginAttempts(0);
        account.setLastLoginAt(Instant.now());
        account.setLocked(false);
        account.setLockedUntil(null);
        
        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Log account authentication success
        log.info("event=account_authenticated account_id={}", updatedAccount.getId());

        // Extract user agent and device name
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceName = request.getDeviceName();

        // Extract IP address and location
        String ipAddress = clientIpResolver.extractClientIp(httpRequest);
        String location = geoLocationService.resolveLocation(ipAddress);

        // Build session creation request
        SessionCreationRequest session = SessionCreationRequest.builder()
                .accountId(account.getId())
                .expiresAt(null)
                .userAgent(userAgent)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .lastIpAddress(ipAddress)
                .location(location)
                .build();
        
        // Create a new session
        Session createdSession = sessionService.createSession(session);

        // Retreve ID of newly created session to issue tokens
        UUID sessionId = createdSession.getId();

        // Issue refresh and access tokens on successful login
        AuthenticationTokens response = tokenService.issueRefreshAndAccessTokens(updatedAccount, sessionId);

        // Return refresh and access tokens
        return response;
    }

    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 CREATION AND AUTHENTICATION
    // ----------------------------------------------------------------

    /**
     * Creates a new account with the registration data from Google.
     * 
     * <p>Creates a new account with the registration data from Google OAuth2 
     * authentication.</p>
     * 
     * @param request Account's information and user's profile from Google
     * @return Created account entity
     */
    private Account createGoogleOAuth2Account(OAuth2AuthenticationRequest request) {
        // Create a new account with Google OAuth2 details
        Account account = Account.builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(AccountRole.USER)
                .oauth2Provider("google")
                .oauth2ProviderId(request.getProviderId())
                .emailVerified(true)
                .enabled(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();

        // Persist created account
        Account createdAccount = accountRepository.save(account);

        // Log account creation success
        log.info("event=oauth2_account_created account_id={}", createdAccount.getId());
        
        // Return created account
        return createdAccount;
    }

    /**
     * Processes Google OAuth2 authentication (creates or links account).
     * 
     * <p>Creates a new account if none exists, or links an existing account
     * with Google OAuth2 credentials. Generates refresh and access tokens
     * for the authenticated account. Publishes an event, so the Session
     * microservice creates a session linked to the account.</p>
     *
     * @param request Account's information and user's profile from Google
     * @return Refresh and access tokens
     * @throws AccountDisabledException If account is disabled
     * @throws EmailNotVerifiedException If account's email is not verified
     * @throws AccountDeletedException If account is deleted
     * @throws AccountLockedException If account is currently locked
     */
    public AuthenticationTokens processGoogleOAuth2Account(
        OAuth2AuthenticationRequest request,
        HttpServletRequest httpRequest
    ) {
        // Look up account by OAuth2 credentials or email
        Account account = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", request.getProviderId())
            .or(() -> accountRepository.findByEmail(request.getEmail())
                .map(existingAccount -> {
                    // Link existing account with Google
                    existingAccount.setOauth2Provider("google");
                    existingAccount.setOauth2ProviderId(request.getProviderId());
                    existingAccount.setEmailVerified(true);
                    Account updatedExistingAccount = accountRepository.save(existingAccount);
                    log.info("event=oauth2_account_linked account_id={} provider=google", updatedExistingAccount.getId());
                    return updatedExistingAccount;
                })
            )
            // Create new account if not found
            .orElseGet(() -> createGoogleOAuth2Account(request));
        
        // Log account authentication attempt
        log.info("event=oauth2_account_authentication_attempt email_id={}", request.getEmail());

        // Ensure account is enabled
        if (!account.getEnabled()) {
            throw new AccountDisabledException();
        }

        // Ensure account is verified
        if (!account.getEmailVerified()) {
            throw new EmailNotVerifiedException(account.getEmail());
        }
        
        // Ensure account is not locked
        if (account.getLocked()) {
            if (account.getLockedUntil() != null && account.getLockedUntil().isBefore(Instant.now())) {
                // Auto-unlock account for expired locks
                account.setLocked(false);
                account.setLockedUntil(null);
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
                log.info("evento=oauth2_account_auto_unlocked account_id={}", account.getId());
            } else {
                // Communicate remaining lock time
                Duration remainingLockTime = Duration.between(Instant.now(), account.getLockedUntil());
                throw new AccountLockedException(String.valueOf(remainingLockTime.toMinutes()));
            }
        }

        // Reactivate account if was soft deleted
        if (account.getDeleted()) {
            account.setDeleted(false);
        }
        
        // Update last login time
        account.setLastLoginAt(Instant.now());

        // Persist updated account
        Account savedAccount = accountRepository.save(account);

        // Log account authentication success
        log.info("event=oauth2_account_authenticated account_id={}", savedAccount.getId());

        // Extract IP address and user agent
        String ipAddress = clientIpResolver.extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Build session creation request
        SessionCreationRequest session = SessionCreationRequest.builder()
                .accountId(account.getId())
                .expiresAt(null)
                .userAgent(userAgent)
                .deviceName(request.getDeviceName())
                .ipAddress(ipAddress)
                .lastIpAddress(ipAddress)
                .build();
        
        // Create a new session
        Session createdSession = sessionService.createSession(session);

        // Retreve ID of newly created session to issue tokens
        UUID sessionId = createdSession.getId();

        // Issue refresh and access tokens for successful Google OAuth2 login
        AuthenticationTokens response = tokenService.issueRefreshAndAccessTokens(savedAccount, sessionId);

        // Return refresh and access tokens
        return response;
    }

    // ----------------------------------------------------------------
    // DEAUTHENTICATION
    // ----------------------------------------------------------------

    /**
     * Deauthenticates an account from a session.
     * 
     * <p>Revokes the associated refresh token during logout and updates the
     * account's last logout timestamp. Terminates the session linked to the
     * refresh token.</p>
     * 
     * @param request Refresh token value linked to account
     * @throws InvalidRefreshTokenException If refresh token is invalid
     */
    public void deauthenticateAccount(RefreshTokenRequest request) {
        // Log account deauthentication attempt
        log.info("event=account_deauthentication_attempt refresh_token={}", request.getValue());

        // Extract refresh token value from request
        String value = request.getValue();

        // Look up token by value and type
        Token refreshToken = tokenService.findTokenByValueAndType(value, TokenType.REFRESH);

        // Ensure refresh token is valid
        if(!tokenService.isTokenValid(refreshToken)) {
            throw new InvalidTokenException(TokenType.REFRESH);
        }

        // Get account linked to refresh token
        Account account = refreshToken.getAccount();

        // Update last logout timestamp
        account.setLastLogoutAt(Instant.now());

        // Persist updated account
        accountRepository.save(account);

        // Revoke refresh token
        tokenService.revokeTokenByValue(value);

        // Retrieve session's ID
        UUID sessionId = refreshToken.getSessionId();

        // Define session termination reason
        SessionTerminationReason terminationReason = SessionTerminationReason.LOGOUT;

        // Terminate the associated session
        sessionService.terminateSessionById(sessionId, terminationReason);

        // Log account deauthentication success
        log.info("event=account_deauthenticated account_id={}", account.getId());
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Verifies email using the email verification token.
     *
     * <p>Sets an account's email as verified if the token is valid and the
     * account is not already verified. Revokes the used email verification
     * token.</p>
     * 
     * @param request Email verification token value linked to account
     * @return Verified account's information
     * @throws InvalidEmailVerificationTokenException If email verification 
     * token is invalid
     * @throws EmailAlreadyRegisteredException If email is already verified
     */
    public AccountInfoResponse verifyEmail(EmailVerificationRequest request) {
        // Log email verification attempt
        log.info("event=email_verification_attempt token={}", request.getValue());

        // Extract email verification token value from request
        String value = request.getValue();

        // Look up token by value and type
        Token emailVerificationToken = tokenService.findTokenByValueAndType(value, TokenType.EMAIL_VERIFICATION);

        // Ensure email verification token is valid
        if(!tokenService.isTokenValid(emailVerificationToken)) {
            throw new InvalidTokenException(TokenType.EMAIL_VERIFICATION);
        }

        // Get account linked to email verification token
        Account account = emailVerificationToken.getAccount();

        // Check if email is not already verified
        if(account.getEmailVerified()) {
            throw new EmailAlreadyVerifiedException(account.getEmail());
        }

        // Set email as verified
        account.setEmailVerified(true);

        // Persist updated account
        accountRepository.save(account);

        // Log email verification success
        log.info("event=email_verified account_id={}", account.getId());

        // Revoke email verification token used
        tokenService.revokeTokenByValue(value);

        // Map account's information from verified account
        AccountInfoResponse accountInfo = accountMapper.toAccountInfoDTO(account);

        // Return verified account's information
        return accountInfo;
    }

    /**
     * Resends email verification link.
     * 
     * <p>Revokes old email verification tokens and issues a new one if an
     * account is not already verified. Publishes an event to send the new
     * email verification token using the Email microservice.</p>
     *
     * @param request Email address to send email verification link
     * @return Message indicating if email verification link has been sent
     */
    public GenericMessageResponse resendEmailVerification(EmailResendRequest request) {
        // Log email verification resend attempt
        log.info("event=email_verification_resend_attempt email={}", request.getEmail());

        // Look up account by email
        accountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            // Ensure account is not verified before issuing new token
            if (!account.getEmailVerified()) {
                // Revoke old email verification tokens
                tokenService.revokeAllTokensByAccountIdAndType(account.getId(), TokenType.EMAIL_VERIFICATION);
                
                // Issue new email verification token
                tokenService.issueToken(account, TokenType.EMAIL_VERIFICATION, EMAIL_TOKEN_LIFETIME_HOURS);
                
                // Log email verification resend success
                log.info("event=email_verification_resent account_id={}", account.getId());
                
                // Publish event for Notification microservice to send verification link
                //
            }
        });

        // Build response with email verification message
        GenericMessageResponse response = GenericMessageResponse.builder()
                .message("If your email is registered, a verification link has been sent.")
                .build();

        // Return message indicating if email verification link has been sent
        return response;
    }

}