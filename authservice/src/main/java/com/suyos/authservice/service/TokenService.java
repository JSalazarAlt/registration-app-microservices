package com.suyos.authservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.AuthenticationResponseDTO;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {
    
    /** Repository for refresh token operations */
    private final TokenRepository tokenRepository;

    /** JWT service for token generation and validation */
    private final JwtService jwtService;

    public AuthenticationResponseDTO issueTokens(Account account) {
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(new ArrayList<>())
                .build();

        // Generate a new access token
        String accessToken = jwtService.generateToken(userDetails);

        // Generate a new refresh token
        String refreshToken = UUID.randomUUID().toString();

        // Generate a new Token entity
        Token token = new Token();
        token.setToken(refreshToken);
        token.setAccount(account);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(30));
        token.setRevoked(false);
        tokenRepository.save(token);

        // Return the authentication's profile DTO
        return AuthenticationResponseDTO.builder()
                .accountId(account.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }


    @Transactional
    public AuthenticationResponseDTO refreshToken(String refreshToken) {
        // Fetch if there is an existing token for the refresh token
        Token token = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        // Check if the token is revoked or expired
        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // Revoke old token
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // Issue new tokens (refresh token rotation)
        return issueTokens(token.getAccount());
    }

    public void revokeToken(String refreshToken) {
        // Fetch if there is an existing token for the refresh token
        Token token = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        // Set the revoked and the revokedAt fields
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());

        // Persist the updated token
        tokenRepository.save(token);
    }

}