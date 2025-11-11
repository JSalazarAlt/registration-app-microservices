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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private AccountMapper accountMapper;
    
    @Mock
    private LoginAttemptService loginAttemptService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private TokenService tokenService;
    
    @Mock
    private UserClient userClient;
    
    @InjectMocks
    private AuthService authService;
    
    private Account testAccount;
    private RegistrationRequestDTO registrationDTO;
    private AuthenticationRequestDTO loginDTO;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();
                
        registrationDTO = RegistrationRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
                
        loginDTO = AuthenticationRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void createAccount_Success() {
        // Given
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

        // When
        AccountInfoDTO result = authService.createAccount(registrationDTO);

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getUsername(), result.getUsername());
        assertEquals(testAccount.getEmail(), result.getEmail());
        verify(accountRepository).save(any());
        verify(userClient).createUser(any());
    }

    @Test
    void createAccount_EmailAlreadyExists() {
        // Given
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.createAccount(registrationDTO));
        assertEquals("Email already registered", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void authenticateAccount_Success() {
        // Given
        when(accountRepository.findActiveByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(accountRepository.save(any())).thenReturn(testAccount);
        when(tokenService.issueRefreshAndAccessTokens(any())).thenReturn(
            AuthenticationResponseDTO.builder()
                .accountId(testAccount.getId())
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build()
        );

        // When
        AuthenticationResponseDTO result = authService.authenticateAccount(loginDTO);

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getId(), result.getAccountId());
        assertNotNull(result.getAccessToken());
        verify(accountRepository).save(any());
    }

    @Test
    void authenticateAccount_InvalidPassword() {
        // Given
        when(accountRepository.findActiveByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateAccount(loginDTO));
        assertEquals("Invalid email or password", exception.getMessage());
        verify(loginAttemptService).recordFailedAttempt(testAccount);
    }

    @Test
    void authenticateAccount_AccountNotFound() {
        // Given
        when(accountRepository.findActiveByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.authenticateAccount(loginDTO));
        assertTrue(exception.getMessage().contains("Account not found"));
    }
    
}