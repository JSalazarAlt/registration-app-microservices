package com.suyos.authservice.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.dto.request.EmailResendRequestDTO;
import com.suyos.authservice.dto.request.EmailVerificationRequestDTO;
import com.suyos.authservice.dto.request.OAuth2AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.dto.response.GenericMessageResponseDTO;
import com.suyos.authservice.event.AccountEventProducer;
import com.suyos.authservice.exception.exceptions.AccountDeletedException;
import com.suyos.authservice.exception.exceptions.AccountDisabledException;
import com.suyos.authservice.exception.exceptions.AccountLockedException;
import com.suyos.authservice.exception.exceptions.EmailNotVerifiedException;
import com.suyos.authservice.exception.exceptions.EmailAlreadyRegisteredException;
import com.suyos.authservice.exception.exceptions.InvalidCredentialsException;
import com.suyos.authservice.exception.exceptions.InvalidPasswordException;
import com.suyos.authservice.exception.exceptions.InvalidTokenException;
import com.suyos.authservice.exception.exceptions.UsernameAlreadyTakenException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Role;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;
import com.suyos.common.event.UserCreationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for authentication-related operations.
 *
 * <p>Handles account creation, authentication, OAuth2 and MFA integration,
 * and email verification. Communicates with the User, Email, and Session
 * microservices. Uses {@link TokenService} for token generation and 
 * revocation for authentication and email verification.</p>
 *
 * @author Joel Salazar
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

    /** Service for handling failed login attempts and account locking */
    private final LoginAttemptService loginAttemptService;
    
    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /** Email verification token lifetime in hours */
    private static final Long EMAIL_TOKEN_LIFETIME_HOURS = 24L;

    /** Refresh token lifetime in hours */
    private static final Long REFRESH_TOKEN_LIFETIME_HOURS = 720L;

    // ----------------------------------------------------------------
    // TRADITIONAL REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * Creates a new account with the registration data.
     * 
     * 
     * <p>Registers a new account if the username and email are not already
     * in use. After creation, publishes a request to the User microservice
     * to create a corresponding user record.</p>
     * 
     * @param request Registration data containing account's credentials and
     * user's profile
     * @return Created account's information
     * @throws UsernameAlreadyTakenException If username is already in use
    * @throws EmailAlreadyRegisteredException If email is already registered
     */
    public AccountInfoDTO createAccount(RegistrationRequestDTO request) {
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
        account.setRole(Role.USER);
        
        // Persist created account
        Account createdAccount = accountRepository.save(account);

        // Log account creation
        log.info("event=account_created account_id={}", createdAccount.getId());

        // Issue email verification token
        tokenService.issueToken(createdAccount, TokenType.EMAIL_VERIFICATION, EMAIL_TOKEN_LIFETIME_HOURS);

        // Build user creation event
        UserCreationEvent event = UserCreationEvent.builder()
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
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(createdAccount);
    
        // Return created account's information
        return accountInfo;
    }

    /**
     * Authenticates account credentials.
     * 
     * <p>Verifies an account using login credentials if enabled, verified,
     * active, and not locked. Updates login tracking fields and issues new
     * refresh and access tokens on successful authentication.</p>
     * 
     * @param request Login credentials
     * @return Refresh and access tokens
     * @throws InvalidCredentialsException If username or email does not match any account
     * @throws AccountDisabledException If account is disabled
     * @throws EmailNotVerifiedException If account's email is not verified
     * @throws AccountDeletedException If account is deleted
     * @throws AccountLockedException If account is currently locked
     * @throws InvalidPasswordException If provided password is incorrect
     */
    public AuthenticationResponseDTO authenticateAccount(AuthenticationRequestDTO request) {
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

        // Ensure account is not deleted
        if (account.getDeleted()) {
            throw new AccountDeletedException();
        }

        // Ensure account is not locked
        if (account.getLocked()) {
            if (account.getLockedUntil() != null && account.getLockedUntil().isBefore(Instant.now())) {
                // Auto-unlock expired locks
                account.setLocked(false);
                account.setLockedUntil(null);
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
            } else {
                Duration remainingLockTime = Duration.between(Instant.now(), account.getLockedUntil());
                throw new AccountLockedException(String.valueOf(remainingLockTime.toMinutes()));
            }
        }
        
        // Verify password match
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            loginAttemptService.recordFailedAttempt(account);
            throw new InvalidPasswordException();
        }
        
        // Update login tracking fields on successful login
        account.setFailedLoginAttempts(0);
        account.setLastLoginAt(Instant.now());
        account.setLocked(false);
        account.setLockedUntil(null);
        
        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Log account authentication
        log.info("event=account_authenticated account_id={}", updatedAccount.getId());
        
        // Issue refresh and access tokens on successful login
        AuthenticationResponseDTO response = tokenService.issueRefreshAndAccessTokens(updatedAccount);

        // Return refresh and access tokens
        return response;
    }

    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * Creates a new account with the registration data from Google.
     * 
     * <p>Creates a new account with the registration data from Google OAuth2 
     * authentication.</p>
     * 
     * @param request Registration data from Google
     * @return Created account entity
     */
    private Account createGoogleOAuth2Account(OAuth2AuthenticationRequestDTO request) {
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

        // Persist created account
        Account createdAccount = accountRepository.save(account);

        // Log account creation
        log.info("event=oauth2_account_created account_id={}", createdAccount.getId());
        
        // Return created account
        return createdAccount;
    }

    /**
     * Processes Google OAuth2 authentication (creates or links account).
     * 
     * <p>Creates a new account if none exists, or links an existing account
     * with Google OAuth2 credentials. Generates refresh and access tokens
     * for the authenticated account.</p>
     *
     * @param request Login credentials from Google
     * @return Refresh and access tokens
     * @throws AccountDisabledException If account is disabled
     * @throws EmailNotVerifiedException If account's email is not verified
     * @throws AccountDeletedException If account is deleted
     * @throws AccountLockedException If account is currently locked
     */
    public AuthenticationResponseDTO processGoogleOAuth2Account(OAuth2AuthenticationRequestDTO request) {
        // Look up account by OAuth2 credentials or email
        Account account = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", request.getProviderId())
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

        // Ensure account is enabled
        if (!account.getEnabled()) {
            throw new AccountDisabledException();
        }

        // Ensure account is verified
        if (!account.getEmailVerified()) {
            throw new EmailNotVerifiedException(account.getEmail());
        }

        // Ensure account is not deleted
        if (account.getDeleted()) {
            throw new AccountDeletedException();
        }

        // Ensure account is not locked
        if (account.getLocked()) {
            if (account.getLockedUntil() != null && account.getLockedUntil().isBefore(Instant.now())) {
                // Auto-unlock expired locks
                account.setLocked(false);
                account.setLockedUntil(null);
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
            } else {
                Duration remainingLockTime = Duration.between(Instant.now(), account.getLockedUntil());
                throw new AccountLockedException(String.valueOf(remainingLockTime.toMinutes()));
            }
        }
        
        // Update last login time
        account.setLastLoginAt(Instant.now());

        // Persist updated account
        Account savedAccount = accountRepository.save(account);

        // Log account authentication
        log.info("event=oauth2_account_authenticated account_id={}", savedAccount.getId());

        // Issue refresh and access tokens for successful Google OAuth2 login
        AuthenticationResponseDTO response = tokenService.issueRefreshAndAccessTokens(savedAccount);

        // Return refresh and access tokens
        return response;
    }

    // ----------------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------------

    /**
     * Deauthenticates an account.
     * 
     * <p>Revokes the associated refresh token during logout and updates the
     * account's last logout timestamp.</p>
     * 
     * @param request Refresh token value linked to account
     * @throws InvalidRefreshTokenException If refresh token is invalid
     */
    public void deauthenticateAccount(RefreshTokenRequestDTO request) {
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

        // Log account logout
        log.info("event=account_logged_out account_id={}", account.getId());
        
        // Revoke refresh token
        tokenService.revokeTokenByValue(value);
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
     * @param request Email verification token value
     * @return Verified account's information
     * @throws InvalidEmailVerificationTokenException If email verification 
     * token is invalid
     * @throws EmailAlreadyRegisteredException If email is already verified
     */
    public AccountInfoDTO verifyEmail(EmailVerificationRequestDTO request) {
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
            throw new EmailAlreadyRegisteredException(account.getEmail());
        }

        // Set email as verified
        account.setEmailVerified(true);

        // Persist updated account
        accountRepository.save(account);

        // Log email verification
        log.info("event=email_verified account_id={}", account.getId());

        // Revoke email verification token used
        tokenService.revokeTokenByValue(value);

        // Map account's information from verified account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);

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
    public GenericMessageResponseDTO resendEmailVerification(EmailResendRequestDTO request) {
        // Look up account by email
        accountRepository.findByEmail(request.getEmail()).ifPresent(account -> {
            // Ensure account is not verified before issuing new token
            if (!account.getEmailVerified()) {
                // Revoke old email verification tokens
                tokenService.revokeAllTokensByAccountIdAndType(account.getId(), TokenType.EMAIL_VERIFICATION);
                
                // Issue new email verification token
                tokenService.issueToken(account, TokenType.EMAIL_VERIFICATION, REFRESH_TOKEN_LIFETIME_HOURS);
                
                // Log email verification resend
                log.info("event=email_verification_resent account_id={}", account.getId());
                
                // Publish event for Email microservice to send verification link
                //
            }
        });

        // Build response with email verification message
        GenericMessageResponseDTO response = GenericMessageResponseDTO.builder()
                .message("If your email is registered, a verification link has been sent.")
                .build();

        // Return message indicating if email verification link has been sent
        return response;
    }

}