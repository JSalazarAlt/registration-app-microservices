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
import com.suyos.authservice.dto.request.AccountLoginDTO;
import com.suyos.authservice.dto.request.AccountRegistrationDTO;
import com.suyos.authservice.dto.request.TokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.TokenService;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthService authService;
    
    @MockitoBean
    private TokenService tokenService;
    
    private AccountRegistrationDTO registrationDTO;
    private AccountLoginDTO loginDTO;
    private TokenRequestDTO tokenRequestDTO;
    private AccountInfoDTO accountInfoDTO;
    private AuthenticationResponseDTO authResponseDTO;

    @BeforeEach
    void setUp() {
        UUID accountId = UUID.randomUUID();
        
        registrationDTO = AccountRegistrationDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
                
        loginDTO = AccountLoginDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();
                
        tokenRequestDTO = TokenRequestDTO.builder()
                .value("refresh-token")
                .build();
                
        accountInfoDTO = AccountInfoDTO.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .build();
                
        authResponseDTO = AuthenticationResponseDTO.builder()
                .accountId(accountId)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(900L)
                .build();
    }

    @Test
    void registerAccount_Success() throws Exception {
        when(authService.createAccount(any())).thenReturn(accountInfoDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
                
        verify(authService).createAccount(any());
    }

    @Test
    void loginAccount_Success() throws Exception {
        when(authService.authenticateAccount(any())).thenReturn(authResponseDTO);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
                
        verify(authService).authenticateAccount(any());
    }

    @Test
    void logoutAccount_Success() throws Exception {
        doNothing().when(authService).deauthenticateAccount(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequestDTO)))
                .andExpect(status().isNoContent());
                
        verify(authService).deauthenticateAccount(any());
    }

    @Test
    void refreshToken_Success() throws Exception {
        when(tokenService.refreshToken(anyString())).thenReturn(authResponseDTO);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
                
        verify(tokenService).refreshToken("refresh-token");
    }
    
}