package com.suyos.authservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.AuthenticationResponseDTO;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for JWT and refresh token management operations.
 *
 * <p>Handles token generation, refresh, and revocation for authentication
 * flows. Implements refresh token rotation for enhanced security.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {
    
    /** Repository for refresh token operations */
    private final TokenRepository tokenRepository;

    /** JWT service for token generation and validation */
    private final JwtService jwtService;

    /**
     * Issues new JWT and refresh tokens for authenticated account.
     * 
     * @param account Authenticated account
     * @return Authentication response with tokens and expiration info
     */
    public AuthenticationResponseDTO issueTokens(Account account) {
        // Generate a new access token
        String accessToken = jwtService.generateToken(account);

        // Generate a new refresh token
        String refreshToken = UUID.randomUUID().toString();

        // Create and persist refresh token entity
        Token token = new Token();
        token.setToken(refreshToken);
        token.setAccount(account);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(30));
        token.setRevoked(false);
        tokenRepository.save(token);

        // Return authentication response with tokens
        return AuthenticationResponseDTO.builder()
                .accountId(account.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    /**
     * Refreshes JWT token using valid refresh token.
     * 
     * @param refreshToken Current refresh token
     * @return New authentication response with rotated tokens
     * @throws RuntimeException If refresh token is invalid or expired
     */
    public AuthenticationResponseDTO refreshToken(String refreshToken) {
        // Fetch existing token for the refresh token
        Token token = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        // Check if the token is revoked or expired
        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // Revoke old token for security
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // Issue new tokens (refresh token rotation)
        return issueTokens(token.getAccount());
    }

    /**
     * Revokes a refresh token during logout.
     * 
     * @param refreshToken Refresh token to revoke
     * @throws RuntimeException If refresh token is invalid
     */
    public void revokeToken(String refreshToken) {
        // Fetch existing token for the refresh token
        Token token = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        // Mark token as revoked with timestamp
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());

        // Persist the revoked token
        tokenRepository.save(token);
    }
    
    /**
     * Extracts account ID from the Authorization header containing JWT.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return Account ID extracted from the token
     */
    public UUID getAccountIdFromAccessToken(String authHeader) {
        // Strip "Bearer " prefix
        String token = authHeader.replace("Bearer ", "").trim();

        // Extract the account ID from the JWT
        String accountIdStr = jwtService.extractSubject(token);
        UUID accountId = UUID.fromString(accountIdStr);

        // Return the account ID
        return accountId;
    }

}