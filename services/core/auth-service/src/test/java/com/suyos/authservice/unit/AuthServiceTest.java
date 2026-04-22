package com.suyos.authservice.unit;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.suyos.authservice.dto.internal.AuthenticationTokens;
import com.suyos.authservice.dto.internal.SessionCreationRequest;
import com.suyos.authservice.dto.request.AuthenticationRequest;
import com.suyos.authservice.dto.request.EmailResendRequest;
import com.suyos.authservice.dto.request.EmailVerificationRequest;
import com.suyos.authservice.dto.request.RefreshTokenRequest;
import com.suyos.authservice.dto.request.RegistrationRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.dto.response.GenericMessageResponse;
import com.suyos.authservice.event.AccountEventProducer;
import com.suyos.authservice.exception.exceptions.AccountDisabledException;
import com.suyos.authservice.exception.exceptions.AccountLockedException;
import com.suyos.authservice.exception.exceptions.DuplicateRequestException;
import com.suyos.authservice.exception.exceptions.EmailAlreadyRegisteredException;
import com.suyos.authservice.exception.exceptions.EmailNotVerifiedException;
import com.suyos.authservice.exception.exceptions.InvalidCredentialsException;
import com.suyos.authservice.exception.exceptions.InvalidTokenException;
import com.suyos.authservice.exception.exceptions.UsernameAlreadyTakenException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.AccountRole;
import com.suyos.authservice.model.AccountStatus;
import com.suyos.authservice.model.Session;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.GeoLocationService;
import com.suyos.authservice.service.IdempotencyService;
import com.suyos.authservice.service.LoginAttemptService;
import com.suyos.authservice.service.SessionService;
import com.suyos.authservice.service.TokenService;
import com.suyos.authservice.util.ClientIpResolver;
import com.suyos.common.event.UserCreationEvent;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    /** Service under test */
    @InjectMocks
    private AuthService authService;

    /** Mocked account mapper */
    @Mock
    private AccountMapper accountMapper;

    /** Mocked account repository */
    @Mock
    private AccountRepository accountRepository;

    /** Mocked account event producer */
    @Mock
    private AccountEventProducer accountEventProducer;

    /** Mocked token service */
    @Mock
    private TokenService tokenService;

    /** Mocked session service */
    @Mock
    private SessionService sessionService;

    /** Mocked login attempt service */
    @Mock
    private LoginAttemptService loginAttemptService;

    /** Mocked idempotency service */
    @Mock
    private IdempotencyService idempotencyService;

    /** Mocked geolocation service */
    @Mock
    private GeoLocationService geoLocationService;

    /** Mocked client IP address resolver */
    @Mock
    private ClientIpResolver clientIpResolver;
    
    /** Password encoder for secure password hashing */
    @Mock
    private PasswordEncoder passwordEncoder;

    /** Test account */
    private Account testAccount;

    /** Test account's information */
    private AccountInfoResponse testAccountInfo;

    /** Test registration request */
    private RegistrationRequest registrationRequest;

    /** Test authentication request */
    private AuthenticationRequest authenticationRequest;

    /** Test refresh request */
    private RefreshTokenRequest refreshTokenRequest;

    /** Test email verification request */
    private EmailVerificationRequest emailVerificationRequest;

    /** Test email verification request */
    private EmailResendRequest emailResendRequest;

    /** Refresh token lifetime in hours */
    private static final Long REFRESH_TOKEN_LIFETIME_DAYS = 30L;

    /** Email verification token lifetime in hours */
    private static final Long EMAIL_TOKEN_LIFETIME_HOURS = 24L;

    /**
     * Initializes common test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Build test registration request
        registrationRequest = RegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("test123")
                .build();

        // Build test authentication request
        authenticationRequest = AuthenticationRequest.builder()
                .identifier("testuser")
                .password("test123")
                .deviceName("Chrome")
                .build();
        
        // Build test refresh token request
        refreshTokenRequest = RefreshTokenRequest.builder()
                .value("refresh-token")
                .build();
        
        // Build test email verification request
        emailVerificationRequest = EmailVerificationRequest.builder()
                .value("email-verification-token")
                .build();
        
        // Build test email resend request
        emailResendRequest = EmailResendRequest.builder()
                .email("test@example.com")
                .build();

        // Generate test account ID
        UUID accountId = UUID.randomUUID();

        // Build test account
        testAccount = Account.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .password("test123")
                .build();
        
        // Build test account's profile
        testAccountInfo = AccountInfoResponse.builder()
                .id(testAccount.getId())
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    // ----------------------------------------------------------------
    // TRADITIONAL CREATION AND AUTHENTICATION
    // ----------------------------------------------------------------

    /**
     * Creates an account successfully.
     */
    @Test
    void createAccount_Success() {
        // Mock account repository to return false when searching for existent username
        when(accountRepository.existsByUsername(registrationRequest.getUsername()))
                .thenReturn(false);

        // Mock account repository to return false when searching for existent email
        when(accountRepository.existsByEmail(registrationRequest.getEmail()))
                .thenReturn(false);

        // Mock account mapper to return test account when mapping test registration request
        when(accountMapper.createFromRequest(registrationRequest))
                .thenReturn(testAccount);
        
        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);
        
        // Mock account mapper to return test account's information when mapping test account
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);

        // Call service method to create new account
        AccountInfoResponse response = authService.createAccount(registrationRequest, null);

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull()
                .isEqualTo(testAccountInfo);

        // Assert business logic side effects
        assertThat(testAccount.getRole())
                .as("role should be set to USER by default")
                .isEqualTo(AccountRole.USER);
        
        // Verify interactions
        verify(accountRepository).existsByUsername(registrationRequest.getUsername());
        verify(accountRepository).existsByEmail(registrationRequest.getEmail());
        verify(accountMapper).createFromRequest(registrationRequest);
        verify(accountRepository).save(testAccount);
        verify(tokenService).issueToken(
            testAccount,
            TokenType.EMAIL_VERIFICATION,
            EMAIL_TOKEN_LIFETIME_HOURS
        );
        verify(accountEventProducer).publishUserCreation(any(UserCreationEvent.class));
        verify(accountMapper).toResponse(testAccount);
    }

    /**
     * Throws exception when creating an account and request is duplicated.
     */
    @Test
    void createAccount_DuplicateRequest() {
        // Generate idempotency key
        String idempotencyKey = "idem-key";

        // Mock account repository to return true when searching for existent username
        when(idempotencyService.checkAndLock(any(), any()))
                .thenReturn(false);

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.createAccount(registrationRequest, idempotencyKey)).
                isInstanceOf(DuplicateRequestException.class);

        // Verify interactions and no interactions
        verify(idempotencyService).checkAndLock(any(), any());
        verifyNoInteractions(accountRepository, accountMapper, tokenService, accountEventProducer);
    }

    /**
     * Throws exception when creating an account and username is already taken.
     */
    @Test
    void createAccount_UsernameAlreadyTaken() {
        // Mock account repository to return true when searching for existent username
        when(accountRepository.existsByUsername(registrationRequest.getUsername()))
                .thenReturn(true);

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.createAccount(registrationRequest, null))
                .isInstanceOf(UsernameAlreadyTakenException.class);

        // Verify interactions and no interactions
        verify(accountRepository).existsByUsername(registrationRequest.getUsername());
        verify(accountRepository, never()).existsByEmail(registrationRequest.getEmail());
        verify(accountRepository, never()).save(testAccount);
        verifyNoInteractions(accountMapper, tokenService, accountEventProducer);
    }

    /**
     * Throws exception when creating an account and email is already registered.
     */
    @Test
    void createAccount_EmailAlreadyRegistered() {
        // Mock account repository to return true when searching for existent username
        when(accountRepository.existsByUsername(registrationRequest.getUsername()))
                .thenReturn(false);

        // Mock account repository to return true when searching for existent email
        when(accountRepository.existsByEmail(registrationRequest.getEmail()))
                .thenReturn(true);

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.createAccount(registrationRequest, null))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        // Verify interactions and no interactions
        verify(accountRepository).existsByUsername(registrationRequest.getUsername());
        verify(accountRepository).existsByEmail(registrationRequest.getEmail());
        verify(accountRepository, never()).save(testAccount);
        verifyNoInteractions(accountMapper, tokenService, accountEventProducer);
    }

    /**
     * Authenticates an account successfully.
     */
    @Test
    void authenticateAccount_Success() {
        // Build test authentication response
        AuthenticationTokens tokens = AuthenticationTokens.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .build();

        // Build test session
        Session session = Session.builder()
                .id(UUID.randomUUID())
                .build();
        
        // Mock HTTP request for authentication
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        
        // Set test account's state to active, verified, not locked nor deleted
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setEmailVerified(true);
        testAccount.setLocked(false);
        
        // Mock account repository to return false when searching for existent username
        when(accountRepository.findByUsername(testAccount.getUsername()))
            .thenReturn(Optional.of(testAccount));

        // Mock password encoder to return true when password matches
        when(passwordEncoder.matches(authenticationRequest.getPassword(), testAccount.getPassword()))
                .thenReturn(true);
        
        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);
        
        // Mock HTTP request to return Chrome when searching for User-Agent header
        when(httpRequest.getHeader("User-Agent"))
                .thenReturn("Chrome");
        
        // Mock client IP resolver to return 127.0.0.1 when extracting IP from HTTP request
        when(clientIpResolver.extractClientIp(httpRequest))
                .thenReturn("127.0.0.1");

        // Mock geolocation service to return location when processing IP address
        when(geoLocationService.resolveLocation("127.0.0.1"))
                .thenReturn("Internal Network");

        // Mock session service to return test session when creating a session
        when(sessionService.createSession(any(SessionCreationRequest.class)))
                .thenReturn(session);

        // Mock token service to return 
        when(tokenService.issueRefreshAndAccessTokens(testAccount, session.getId()))
                .thenReturn(tokens);

        // Call service method to authenticate account
        AuthenticationTokens response = authService.authenticateAccount(authenticationRequest, httpRequest);

        // Assert expected tokens are returned
        assertThat(response).isEqualTo(tokens);

        // Verify interactions
        verify(accountRepository).findByUsername(testAccount.getUsername());
        verify(passwordEncoder).matches(authenticationRequest.getPassword(), testAccount.getPassword());
        verify(accountRepository).save(testAccount);
        verify(sessionService).createSession(any());
        verify(tokenService).issueRefreshAndAccessTokens(testAccount, session.getId());
    }

    /**
     * Throws exception when authenticating an account and account does not
     * exist for the given identifier.
     */
    @Test
    void authenticateAccount_IdenfierNotFound() {
        // Generate authentication request with non-existent username
        AuthenticationRequest randomAuthenticationRequest = AuthenticationRequest.builder()
                .identifier("random-username")
                .password("test123")
                .deviceName("Chrome")
                .build();

        // Mock HTTP request for authentication
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        
        // Mock account repository to return false when searching for existent username
        when(accountRepository.findByUsername(randomAuthenticationRequest.getIdentifier()))
            .thenReturn(Optional.empty());
        
        // Mock account repository to return empty when searching for non-existent email
        when(accountRepository.findByEmail(randomAuthenticationRequest.getIdentifier()))
                .thenReturn(Optional.empty());

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.authenticateAccount(randomAuthenticationRequest, httpRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        // Verify interactions and no interactions
        verify(accountRepository).findByUsername(randomAuthenticationRequest.getIdentifier());
        verifyNoInteractions(passwordEncoder, tokenService, sessionService);
        verify(accountRepository, never()).save(testAccount);
    }

    /**
     * Throws exception when authenticating an account and account is disabled.
     */
    @Test
    void authenticateAccount_AccountDisabled() {
        // Mock HTTP request for authentication
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        
        // Set test account's state to disabled
        testAccount.setStatus(AccountStatus.DISABLED);
        
        // Mock account repository to return false when searching for existent username
        when(accountRepository.findByUsername(testAccount.getUsername()))
            .thenReturn(Optional.of(testAccount));

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.authenticateAccount(authenticationRequest, httpRequest))
                .isInstanceOf(AccountDisabledException.class);

        // Verify interactions and no interactions
        verify(accountRepository).findByUsername(authenticationRequest.getIdentifier());
        verifyNoInteractions(passwordEncoder, tokenService, sessionService);
        verify(accountRepository, never()).save(testAccount);
    }

    /**
     * Throws exception when authenticating an account and account is not verified.
     */
    @Test
    void authenticateAccount_EmailNotVerified() {
        // Mock HTTP request for authentication
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        
        // Set test account's state to active and not verified
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setEmailVerified(false);
        
        // Mock account repository to return false when searching for existent username
        when(accountRepository.findByUsername(testAccount.getUsername()))
            .thenReturn(Optional.of(testAccount));

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.authenticateAccount(authenticationRequest, httpRequest))
                .isInstanceOf(EmailNotVerifiedException.class);

        // Verify interactions and no interactions
        verify(accountRepository).findByUsername(authenticationRequest.getIdentifier());
        verifyNoInteractions(passwordEncoder, tokenService, sessionService);
        verify(accountRepository, never()).save(testAccount);
    }

    /**
     * Throws exception when authenticating an account and account is locked.
     */
    @Test
    void authenticateAccount_AccountLocked() {
        // Mock HTTP request for authentication
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        // Time to unlock account
        Instant lockedUntil = Instant.now().plus(Duration.ofHours(1));
        
        // Set test account's state to active, verified, and not locked
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setEmailVerified(true);
        testAccount.setLocked(true);
        testAccount.setLockedUntil(lockedUntil);
        
        // Mock account repository to return false when searching for existent username
        when(accountRepository.findByUsername(testAccount.getUsername()))
            .thenReturn(Optional.of(testAccount));

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.authenticateAccount(authenticationRequest, httpRequest))
                .isInstanceOf(AccountLockedException.class);

        // Verify interactions and no interactions
        verify(accountRepository).findByUsername(authenticationRequest.getIdentifier());
        verifyNoInteractions(tokenService, sessionService);
        verify(accountRepository, never()).save(testAccount);
    }

    /**
     * Throws exception when authenticating an account and password is invalid.
     */
    @Test
    void authenticateAccount_WrongPassword() {
        // Generate authentication request with wrong password
        AuthenticationRequest wrongAuthenticationRequest = AuthenticationRequest.builder()
                .identifier("testuser")
                .password("wrong-password")
                .deviceName("Chrome")
                .build();

        // Mock HTTP request for authentication
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        // Set test account's state to active, verified, and not locked
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setEmailVerified(true);
        testAccount.setLocked(false);
        
        // Mock account repository to return false when searching for existent username
        when(accountRepository.findByUsername(testAccount.getUsername()))
            .thenReturn(Optional.of(testAccount));
        
        // Mock password encoder to return false when password matches
        when(passwordEncoder.matches(wrongAuthenticationRequest.getPassword(), testAccount.getPassword()))
                .thenReturn(false);

        // Assert expected exception is thrown
        assertThatThrownBy(() -> authService.authenticateAccount(wrongAuthenticationRequest, httpRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        // Verify interactions and no interactions
        verify(accountRepository).findByUsername(wrongAuthenticationRequest.getIdentifier());
        verify(passwordEncoder).matches(wrongAuthenticationRequest.getPassword(), testAccount.getPassword());
        verify(loginAttemptService).recordFailedAttempt(testAccount);
        verifyNoInteractions(tokenService, sessionService);
        verify(accountRepository, never()).save(testAccount);
    }

    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 CREATION AND AUTHENTICATION
    // ----------------------------------------------------------------

    // ----------------------------------------------------------------
    // DEAUTHENTICATION
    // ----------------------------------------------------------------

    /**
     * Authenticates an account successfully.
     */
    @Test
    void deauthenticateAccount_Success() {
        // Generate session ID for test session
        UUID sessionId = UUID.randomUUID();

        // Define expire time for refresh token
        Instant expiresAt = Instant.now().plus(Duration.ofDays(REFRESH_TOKEN_LIFETIME_DAYS));

        // Build test refresh token
        Token refreshToken = Token.builder()
                .value("refresh-token")
                .type(TokenType.REFRESH)
                .account(testAccount)
                .sessionId(sessionId)
                .revoked(false)
                .expiresAt(expiresAt)
                .build();
                
        // Mock token service to return token when searching by value and type
        when(tokenService.findTokenByValueAndType(refreshTokenRequest.getValue(), TokenType.REFRESH))
                .thenReturn(refreshToken);
        
        // Mock token service to return true when validating refresh token
        when(tokenService.isTokenValid(refreshToken)).
                thenReturn(true);

        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);

        // Call service method to deauthenticate account
        authService.deauthenticateAccount(refreshTokenRequest);

        // Capture saved account to assert business state changes
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        // Assert last logout at field is set
        assertThat(captor.getValue().getLastLogoutAt()).isNotNull();

        // Verify interactions
        verify(tokenService).findTokenByValueAndType(refreshTokenRequest.getValue(), TokenType.REFRESH);
        verify(tokenService).isTokenValid(refreshToken);
        verify(tokenService).revokeTokenByValue(refreshTokenRequest.getValue());
        verify(sessionService).terminateSessionById(sessionId, SessionTerminationReason.LOGOUT);
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Verifies an email successfully.
     */
    @Test
    void verifyEmail_Success() {
        // Define expire time for email verification token
        Instant expiresAt = Instant.now().plus(Duration.ofDays(REFRESH_TOKEN_LIFETIME_DAYS));

        // Build test email verification token
        Token emailVerificationToken = Token.builder()
                .value("email-verification-token")
                .type(TokenType.EMAIL_VERIFICATION)
                .account(testAccount)
                .revoked(false)
                .expiresAt(expiresAt)
                .build();
        
        // Set test account's state to not verified
        testAccount.setEmailVerified(false);

        // Mock token service to return token when searching by value and type
        when(tokenService.findTokenByValueAndType(emailVerificationRequest.getValue(), TokenType.EMAIL_VERIFICATION))
                .thenReturn(emailVerificationToken);
        
        // Mock token service to return true when validating email verification token
        when(tokenService.isTokenValid(emailVerificationToken)).
                thenReturn(true);

        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);

         // Mock account repository to return test account when saved
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);

        // Call service method to verify email
        AccountInfoResponse response = authService.verifyEmail(emailVerificationRequest);

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull()
                .isEqualTo(testAccountInfo);

        // Assert email is verified
        assertThat(testAccount.getEmailVerified())
                .as("email should be verified after successful verification")
                .isTrue();

        // Verify interactions and no interactions
        verify(tokenService).findTokenByValueAndType(emailVerificationRequest.getValue(), TokenType.EMAIL_VERIFICATION);
        verify(tokenService).isTokenValid(emailVerificationToken);
        verify(accountRepository).save(testAccount);
        verify(tokenService).revokeTokenByValue(emailVerificationRequest.getValue());
        verify(accountMapper).toResponse(testAccount);
    }

    /**
     * Throws exception when verifying an email and email verification token
     * is not found.
     */
    @Test
    void verifyEmail_TokenNotFound() {
        // Build email verification request with non-existent token value
        EmailVerificationRequest randomEmailVerificationRequest = EmailVerificationRequest.builder()
                .value("random-email-verification-token")
                .build();
        
        // Mock token service to return null when searching by value and type
        when(tokenService.findTokenByValueAndType(randomEmailVerificationRequest.getValue(), TokenType.EMAIL_VERIFICATION))
                .thenReturn(null);

        // Assert expected account's information is returned
        assertThatThrownBy(() -> authService.verifyEmail(randomEmailVerificationRequest))
                .isNotNull()
                .isInstanceOf(InvalidTokenException.class);

        // Verify interactions
        verify(tokenService).findTokenByValueAndType(randomEmailVerificationRequest.getValue(), TokenType.EMAIL_VERIFICATION);
        verify(tokenService, never()).isTokenValid(any(Token.class));
        verify(tokenService, never()).revokeTokenByValue(randomEmailVerificationRequest.getValue());
        verifyNoInteractions(accountRepository, accountMapper);
    }

    @Test
    void verifyEmail_InvalidToken() {
        // Define expire time for expired (invalid) email verification token
        Instant expiresAt = Instant.now().minus(Duration.ofHours(1L));

        // Build invalid email verification token
        Token invalidEmailVerificationToken = Token.builder()
                .value("email-verification-token")
                .type(TokenType.EMAIL_VERIFICATION)
                .account(testAccount)
                .revoked(false)
                .expiresAt(expiresAt)
                .build();

        // Mock token service to return token when searching by value and type
        when(tokenService.findTokenByValueAndType(emailVerificationRequest.getValue(), TokenType.EMAIL_VERIFICATION))
                .thenReturn(invalidEmailVerificationToken);
        
        // Assert expected account's information is returned
        assertThatThrownBy(() -> authService.verifyEmail(emailVerificationRequest))
                .isNotNull()
                .isInstanceOf(InvalidTokenException.class);

        // Verify interactions
        verify(tokenService).findTokenByValueAndType(emailVerificationRequest.getValue(), TokenType.EMAIL_VERIFICATION);
        verify(tokenService).isTokenValid(invalidEmailVerificationToken);
        verify(tokenService, never()).revokeTokenByValue(emailVerificationRequest.getValue());
        verifyNoInteractions(accountRepository, accountMapper);
    }

    /**
     * Resends an email verification successfully.
     */
    @Test
    void resendEmailVerification_Success() {
        // Set test account's state to not verified
        testAccount.setEmailVerified(false);
                
        // Mock token service to return token when searching by value and type
        when(accountRepository.findByEmail(emailResendRequest.getEmail()))
                .thenReturn(Optional.of(testAccount));

        // Call service method to deauthenticate account
        GenericMessageResponse response = authService.resendEmailVerification(emailResendRequest);

        // Assert expected message is returned
        assertThat(response)
                .isNotNull()
                .extracting(GenericMessageResponse::getMessage)
                .isEqualTo("If your email is registered, a verification link has been sent.");

        // Verify interactions
        verify(accountRepository).findByEmail(emailResendRequest.getEmail());
        verify(tokenService).revokeAllTokensByAccountIdAndType(testAccount.getId(), TokenType.EMAIL_VERIFICATION);
        verify(tokenService).issueToken(testAccount, TokenType.EMAIL_VERIFICATION, EMAIL_TOKEN_LIFETIME_HOURS);
    }
    
}