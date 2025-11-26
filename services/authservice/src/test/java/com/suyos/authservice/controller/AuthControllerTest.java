package com.suyos.authservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
//import com.suyos.authservice.dto.request.EmailVerificationRequestDTO;
import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.TokenService;

/**
 * Unit tests for AuthController.
 *
 * <p>Tests authentication endpoints using mocked services to verify
 * controller behavior and request/response handling.</p>
 *
 * @author Joel Salazar
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    /** MockMvc for simulating HTTP requests */
    @Autowired
    private MockMvc mockMvc;
    
    /** ObjectMapper for JSON serialization/deserialization */
    @Autowired
    private ObjectMapper objectMapper;
    
    /** Mocked authentication service */
    @MockitoBean
    private AuthService authService;
    
    /** Mocked token service */
    @MockitoBean
    private TokenService tokenService;
    
    /** Test registration request DTO */
    private RegistrationRequestDTO registrationDTO;
    
    /** Test authentication request DTO */
    private AuthenticationRequestDTO loginDTO;
    
    /** Test refresh token request DTO */
    private RefreshTokenRequestDTO refreshTokenRequestDTO;
    
    /** Test email verification request DTO */
    //private EmailVerificationRequestDTO emailVerificationTokenRequestDTO;
    
    /** Test account info response DTO */
    private AccountInfoDTO accountInfoDTO;
    
    /** Test authentication response DTO */
    private AuthenticationResponseDTO authResponseDTO;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test account ID
        UUID accountId = UUID.randomUUID();
        
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
        
        // Build refresh token request DTO
        refreshTokenRequestDTO = RefreshTokenRequestDTO.builder()
                .value("refresh-token")
                .build();
        
        // Build email verification request DTO
        /* 
        emailVerificationTokenRequestDTO = EmailVerificationRequestDTO.builder()
                .value("email-verification-token")
                .build();
        */

        // Build account info response DTO
        accountInfoDTO = AccountInfoDTO.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .build();
        
        // Build authentication response DTO
        authResponseDTO = AuthenticationResponseDTO.builder()
                .accountId(accountId)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresIn(900L)
                .build();
    }

    /**
     * Tests successful account registration.
     * 
     * <p>Verifies that registration endpoint returns 201 Created with
     * account information when valid registration data is provided.</p>
     */
    @Test
    void registerAccount_Success() throws Exception {
        // Mock service to return account info
        when(authService.createAccount(any())).thenReturn(accountInfoDTO);

        // Perform registration request
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
        
        // Verify service was called
        verify(authService).createAccount(any());
    }

    /**
     * Tests successful account login.
     * 
     * <p>Verifies that login endpoint returns 200 OK with access and
     * refresh tokens when valid credentials are provided.</p>
     */
    @Test
    void loginAccount_Success() throws Exception {
        // Mock service to return authentication response
        when(authService.authenticateAccount(any())).thenReturn(authResponseDTO);

        // Perform login request
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
        
        // Verify service was called
        verify(authService).authenticateAccount(any());
    }

    /**
     * Tests successful account logout.
     * 
     * <p>Verifies that logout endpoint returns 204 No Content when
     * valid refresh token is provided.</p>
     */
    @Test
    void logoutAccount_Success() throws Exception {
        // Mock service to do nothing
        doNothing().when(authService).deauthenticateAccount(any());

        // Perform logout request
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDTO)))
                .andExpect(status().isNoContent());
        
        // Verify service was called
        verify(authService).deauthenticateAccount(any());
    }

    /**
     * Tests successful token refresh.
     * 
     * <p>Verifies that refresh endpoint returns 200 OK with new access
     * token when valid refresh token is provided.</p>
     */
    @Test
    void refreshToken_Success() throws Exception {
        // Mock service to return authentication response
        when(tokenService.refreshToken(refreshTokenRequestDTO)).thenReturn(authResponseDTO);

        // Perform refresh request
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
        
        // Verify service was called
        verify(tokenService).refreshToken(refreshTokenRequestDTO);
    }

    /**
     * Tests registration with invalid data (validation error).
     */
    @Test
    void registerAccount_ValidationError() throws Exception {
        RegistrationRequestDTO invalidDTO = RegistrationRequestDTO.builder()
                .username("ab")  // Too short
                .email("invalid-email")  // Invalid format
                .password("weak")  // Too short
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests login with invalid credentials.
     */
    @Test
    void loginAccount_InvalidCredentials() throws Exception {
        when(authService.authenticateAccount(any()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Tests registration with duplicate email.
     */
    @Test
    void registerAccount_EmailAlreadyExists() throws Exception {
        when(authService.createAccount(any()))
                .thenThrow(new RuntimeException("Email already registered"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isInternalServerError());
    }



    /**
     * Tests registration with missing required fields.
     */
    @Test
    void registerAccount_MissingFields() throws Exception {
        RegistrationRequestDTO invalidDTO = RegistrationRequestDTO.builder()
                .username("testuser")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests login with missing password.
     */
    @Test
    void loginAccount_MissingPassword() throws Exception {
        AuthenticationRequestDTO invalidDTO = AuthenticationRequestDTO.builder()
                .identifier("test@example.com")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests refresh token with invalid token.
     */
    @Test
    void refreshToken_InvalidToken() throws Exception {
        when(tokenService.refreshToken(any()))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDTO)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Tests logout with invalid token.
     */
    @Test
    void logoutAccount_InvalidToken() throws Exception {
        doThrow(new RuntimeException("Invalid refresh token"))
                .when(authService).deauthenticateAccount(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDTO)))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Tests registration with weak password.
     */
    @Test
    void registerAccount_WeakPassword() throws Exception {
        RegistrationRequestDTO weakPasswordDTO = RegistrationRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("123")  // Too short
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weakPasswordDTO)))
                .andExpect(status().isBadRequest());
    }
    
}