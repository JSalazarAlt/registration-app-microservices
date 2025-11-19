package com.suyos.authservice.service;

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

import com.suyos.authservice.client.UserClient;
import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.repository.AccountRepository;

import reactor.core.publisher.Mono;

/**
 * Unit tests for AuthService.
 *
 * <p>Tests authentication business logic using mocked dependencies
 * to verify service behavior.</p>
 *
 * @author Joel Salazar
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
    
    /** Mocked user client */
    @Mock
    private UserClient userClient;
    
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
        when(userClient.createUser(any())).thenReturn(Mono.empty());
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
        verify(userClient).createUser(any());
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

        // Attempt authentication and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Account not found"));
    }
    
}