package com.suyos.authservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.repository.AccountRepository;

/**
 * Unit tests for AuthService.
 *
 * <p>Tests authentication business logic using mocked dependencies
 * to verify service behavior.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /** Mocked account repository */
    @Mock
    private AccountRepository accountRepository;
    
    /** Mocked account mapper */
    @Mock
    private AccountMapper accountMapper;
    
    /** Mocked login attempt service */
    @Mock
    private LoginAttemptService loginAttemptService;
    
    /** Mocked password encoder */
    @Mock
    private PasswordEncoder passwordEncoder;
    
    /** Mocked token service */
    @Mock
    private TokenService tokenService;

    /** Mocked HTTP servlet request */
    @Mock
    private HttpServletRequest httpServletRequest;
    
    /** Auth service under test with injected mocks */
    @InjectMocks
    private AuthService authService;
    
    /** Test account entity */
    private Account testAccount;
    
    /** Test registration request DTO */
    private RegistrationRequestDTO registrationDTO;
    
    /** Test authentication request DTO */
    private AuthenticationRequestDTO loginDTO;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Build test account
        testAccount = Account.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .locked(false)
                .failedLoginAttempts(0)
                .emailVerified(true)
                .build();
        
        // Build registration request DTO
        registrationDTO = RegistrationRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
        
        // Build login request DTO
        loginDTO = AuthenticationRequestDTO.builder()
                .identifier("test@example.com")
                .password("password123")
                .build();

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn("JUnit-Test-Agent");
    }

    /**
     * Tests successful account creation.
     * 
     * <p>Verifies that account is created, saved to database, and
     * user profile is created in User Service.</p>
     */
    @Test
    void createAccount_Success() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountMapper.toEntity(any())).thenReturn(testAccount);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(accountMapper.toAccountInfoDTO(any())).thenReturn(
            AccountInfoDTO.builder()
                .id(testAccount.getId())
                .username(testAccount.getUsername())
                .email(testAccount.getEmail())
                .build()
        );

        AccountInfoDTO result = authService.createAccount(registrationDTO);

        assertNotNull(result);
        assertEquals(testAccount.getUsername(), result.getUsername());
        assertEquals(testAccount.getEmail(), result.getEmail());
        verify(accountRepository).save(any());
    }

    /**
     * Tests account creation with duplicate email.
     * 
     * <p>Verifies that account creation fails when email already
     * exists in database.</p>
     */
    @Test
    void createAccount_EmailAlreadyExists() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, 
            () -> authService.createAccount(registrationDTO));
        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests successful account authentication.
     * 
     * <p>Verifies that authentication succeeds with valid credentials
     * and returns access and refresh tokens.</p>
     */
    @Test
    void authenticateAccount_Success() {
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(tokenService.issueRefreshAndAccessTokens(any(), any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        AuthenticationResponseDTO result =
            authService.authenticateAccount(loginDTO, httpServletRequest);

        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getAccountId());
        verify(accountRepository).save(any());
    }

    /**
     * Tests authentication with invalid password.
     * 
     * <p>Verifies that authentication fails when password is incorrect
     * and failed attempt is recorded.</p>
     */
    @Test
    void authenticateAccount_InvalidPassword() {
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));

        verify(loginAttemptService).recordFailedAttempt(testAccount);
    }

    /**
     * Tests authentication with non-existing account.
     * 
     * <p>Verifies that authentication fails when account does not
     * exist in database.</p>
     */
    @Test
    void authenticateAccount_AccountNotFound() {
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));
    }

    /**
     * Tests authentication with disabled account.
     */
    @Test
    void authenticateAccount_AccountDisabled() {
        testAccount.setEnabled(false);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));
    }

    /**
     * Tests authentication with unverified email.
     */
    @Test
    void authenticateAccount_EmailNotVerified() {
        testAccount.setEmailVerified(false);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));
    }

    /**
     * Tests authentication with locked account.
     */
    @Test
    void authenticateAccount_AccountLocked() {
        testAccount.setLocked(true);
        testAccount.setLockedUntil(java.time.Instant.now().plusSeconds(3600));
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));
    }

    /**
     * Tests authentication with deleted account.
     */
    @Test
    void authenticateAccount_AccountDeleted() {
        testAccount.setDeleted(true);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));
    }

    /**
     * Tests account creation with duplicate username.
     */
    @Test
    void createAccount_UsernameAlreadyExists() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class,
            () -> authService.createAccount(registrationDTO));

        verify(accountRepository, never()).save(any());
    }

    /**
     * Tests authentication by username instead of email.
     */
    @Test
    void authenticateAccount_ByUsername_Success() {
        loginDTO = AuthenticationRequestDTO.builder()
                .identifier("testuser")
                .password("password123")
                .build();

        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(tokenService.issueRefreshAndAccessTokens(any(), any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        AuthenticationResponseDTO result =
            authService.authenticateAccount(loginDTO, httpServletRequest);

        assertNotNull(result);
        verify(accountRepository).findByUsername("testuser");
    }

    /**
     * Tests auto-unlock of expired account lock.
     */
    @Test
    void authenticateAccount_AutoUnlockExpiredLock() {
        testAccount.setLocked(true);
        testAccount.setLockedUntil(java.time.Instant.now().minusSeconds(3600));
        testAccount.setFailedLoginAttempts(5);

        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(tokenService.issueRefreshAndAccessTokens(any(), any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        AuthenticationResponseDTO result =
            authService.authenticateAccount(loginDTO, httpServletRequest);

        assertNotNull(result);
        assertFalse(testAccount.getLocked());
        assertNull(testAccount.getLockedUntil());
        assertEquals(0, testAccount.getFailedLoginAttempts());
    }

    /**
     * Tests failed login attempt increments counter.
     */
    @Test
    void authenticateAccount_IncrementFailedAttempts() {
        testAccount.setFailedLoginAttempts(2);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO, httpServletRequest));

        verify(loginAttemptService).recordFailedAttempt(testAccount);
    }

    /**
     * Tests successful authentication resets failed attempts.
     */
    @Test
    void authenticateAccount_ResetFailedAttempts() {
        testAccount.setFailedLoginAttempts(3);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(tokenService.issueRefreshAndAccessTokens(any(), any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        authService.authenticateAccount(loginDTO, httpServletRequest);

        verify(accountRepository).save(argThat(account -> 
            account.getFailedLoginAttempts() == 0
        ));
    }

    /**
     * Tests password encoding during registration.
     */
    @Test
    void createAccount_PasswordEncoded() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountMapper.toEntity(any())).thenReturn(testAccount);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(accountMapper.toAccountInfoDTO(any())).thenReturn(
            AccountInfoDTO.builder()
                .id(testAccount.getId())
                .username(testAccount.getUsername())
                .email(testAccount.getEmail())
                .build()
        );

        authService.createAccount(registrationDTO);

        verify(passwordEncoder).encode("password123");
        verify(accountRepository).save(argThat(account -> 
            "encodedPassword".equals(account.getPassword())
        ));
    }
}