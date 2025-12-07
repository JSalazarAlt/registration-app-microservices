package com.suyos.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.suyos.auth.dto.request.AuthenticationRequestDTO;
import com.suyos.auth.dto.request.RegistrationRequestDTO;
import com.suyos.auth.dto.response.AccountInfoDTO;
import com.suyos.auth.dto.response.AuthenticationResponseDTO;
import com.suyos.auth.mapper.AccountMapper;
import com.suyos.auth.model.Account;
import com.suyos.auth.repository.AccountRepository;

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
                .build();
    }

    /**
     * Tests successful account creation.
     * 
     * <p>Verifies that account is created, saved to database, and
     * user profile is created in User Service.</p>
     */
    @Test
    void createAccount_Success() {
        // Mock dependencies for successful account creation
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

        // Create account
        AccountInfoDTO result = authService.createAccount(registrationDTO);

        // Verify account was created successfully
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
        // Mock email already exists
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        // Attempt to create account and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.createAccount(registrationDTO));
        assertEquals("Email already registered", exception.getMessage());
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
        // Mock dependencies for successful authentication
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(tokenService.issueRefreshAndAccessTokens(any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        // Authenticate account
        AuthenticationResponseDTO result = authService.authenticateAccount(loginDTO);

        // Verify authentication was successful
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getAccountId());
        assertNotNull(result.getAccessToken());
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
        // Mock account found but password doesn't match
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Attempt authentication and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateAccount(loginDTO));
        assertEquals("Invalid email or password", exception.getMessage());
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
        // Mock account not found
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Attempt authentication and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    /**
     * Tests authentication with disabled account.
     */
    @Test
    void authenticateAccount_AccountDisabled() {
        testAccount.setEnabled(false);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Account disabled"));
    }

    /**
     * Tests authentication with unverified email.
     */
    @Test
    void authenticateAccount_EmailNotVerified() {
        testAccount.setEmailVerified(false);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Email not verified"));
    }

    /**
     * Tests authentication with locked account.
     */
    @Test
    void authenticateAccount_AccountLocked() {
        testAccount.setLocked(true);
        testAccount.setLockedUntil(java.time.Instant.now().plusSeconds(3600));
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Account locked"));
    }

    /**
     * Tests authentication with deleted account.
     */
    @Test
    void authenticateAccount_AccountDeleted() {
        testAccount.setDeleted(true);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Account deleted"));
    }

    /**
     * Tests account creation with duplicate username.
     */
    @Test
    void createAccount_UsernameAlreadyExists() {
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByUsername(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.createAccount(registrationDTO));
        assertEquals("Username already registered", exception.getMessage());
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
        when(tokenService.issueRefreshAndAccessTokens(any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        AuthenticationResponseDTO result = authService.authenticateAccount(loginDTO);

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
        when(tokenService.issueRefreshAndAccessTokens(any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        AuthenticationResponseDTO result = authService.authenticateAccount(loginDTO);

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
            () -> authService.authenticateAccount(loginDTO));

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
        when(tokenService.issueRefreshAndAccessTokens(any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        authService.authenticateAccount(loginDTO);

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