package com.suyos.authservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;
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
    
    /** Repository for token operations */
    private final TokenRepository tokenRepository;

    /** Service for JWT token generation and validation */
    private final JwtService jwtService;

    // TOKEN GENERATION

    /**
     * Issues new email verification token for authenticating an email.
     * 
     * @param account Authenticated account
     * @return Email verification token
     */
    public Token issueEmailVerificationToken(Account account) {
        // Generate a new email verification token value
        String value = UUID.randomUUID().toString();

        // Create email verification token
        Token emailVerificationToken = new Token();
        emailVerificationToken.setValue(value);
        emailVerificationToken.setType(TokenType.EMAIL_VERIFICATION);
        emailVerificationToken.setAccount(account);
        emailVerificationToken.setIssuedAt(LocalDateTime.now());
        emailVerificationToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        // Persist created email verification token
        Token createdEmailVerificationToken = tokenRepository.save(emailVerificationToken);

        // Return created email verification token
        return createdEmailVerificationToken;
    }

    /**
     * Issues new refresh and access tokens for an authenticated account.
     * 
     * @param account Authenticated account
     * @return Authentication response with refresh and access tokens
     */
    public AuthenticationResponseDTO issueRefreshAndAccessTokens(Account account) {
        // Generate a new access token
        String accessToken = jwtService.generateToken(account);

        // Generate a new refresh token value
        String value = UUID.randomUUID().toString();

        // Create refresh token
        Token refreshToken = new Token();
        refreshToken.setValue(value);
        refreshToken.setType(TokenType.REFRESH);
        refreshToken.setAccount(account);
        refreshToken.setIssuedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));

        // Persist created refresh token
        tokenRepository.save(refreshToken);

        // Build authentication response DTO with refresh and access tokens
        AuthenticationResponseDTO authenticationResponseDTO = AuthenticationResponseDTO.builder()
                .accountId(account.getId())
                .accessToken(accessToken)
                .refreshToken(value)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        // Return authentication response DTO
        return authenticationResponseDTO;
    }

    // TOKEN VALIDATION

    public boolean isTokenValid(String value) {
        // Return true if token is not revoked and not expired
        return tokenRepository.findValidByValue(value).isPresent();
    }

    // TOKEN REFRESH

    /**
     * Refreshes access token using a valid refresh token (token rotation).
     * 
     * @param value Current refresh token value
     * @return New authentication response with rotated refresh and access tokens
     * @throws RuntimeException If refresh token is invalid or expired
     */
    public AuthenticationResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        // Find if there is an existing refresh token for the value
        Token refreshToken = tokenRepository.findByValue(refreshTokenRequestDTO.getValue())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        // Check if refresh token is revoked or expired
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // Revoke old token for security
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        // Persist revoked token
        tokenRepository.save(refreshToken);

        // Issue new refresh and access tokens (refresh token rotation)
        AuthenticationResponseDTO authenticationResponseDTO = issueRefreshAndAccessTokens(refreshToken.getAccount());

        // Return authentication response DTO with new refresh and access tokens
        return authenticationResponseDTO;
    }

    // TOKEN HELPER METHODS

    /**
     * Extracts account ID from Authorization header containing access token.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return Account ID extracted from access token
     */
    public UUID extractAccountIdFromAccessToken(String authHeader) {
        // Strip "Bearer " prefix
        String accessToken = authHeader.replace("Bearer ", "").trim();

        // Extract account ID from access token
        String accountIdStr = jwtService.extractSubject(accessToken);
        UUID accountId = UUID.fromString(accountIdStr);

        // Return account ID
        return accountId;
    }

    /**
     * Finds account associated with a token.
     * 
     * @param value Token value
     * @return Account associated with token
     * @throws RuntimeException If token is invalid
     */
    public Account findAccountByToken(String value) {
        // Find token by value
        Token token = tokenRepository.findByValue(value)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        // Return account associated with token
        return token.getAccount();
    }

    // TOKEN REVOCATION

    /**
     * Revokes a token.
     * 
     * @param value Token value to revoke
     * @throws RuntimeException If token is invalid
     */
    public void revokeTokenByValue(String value) {
        // Find if there is an existing token by value
        Token refreshToken = tokenRepository.findByValue(value)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        // Mark token as revoked with timestamp
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        // Persist revoked token
        tokenRepository.save(refreshToken);
    }

    /**
     * Revokes all valid tokens for an account (e.g., on password change).
     * 
     * @param accountId Account ID
     */
    public void revokeAllTokensByAccountId(UUID accountId) {
        tokenRepository.revokeAllValidByAccountId(accountId);
    }

    /**
     * Revokes all valid tokens of a specific type for an account (e.g., on 
     * email verification resend).
     * 
     * @param accountId Account ID
     */
    public void revokeAllTokensByAccountIdAndType(UUID accountId, TokenType type) {
        tokenRepository.revokeAllValidByAccountAndType(accountId, type);
    }

    // TOKEN DELETION

    /**
     * Deletes a token.
     * 
     * @param value Token value to delete
     * @throws RuntimeException If token is invalid
     */
    public void deleteToken(String value) {
        // Find if there is an existing token for the value
        Token token = tokenRepository.findByValue(value)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        // Delete token
        tokenRepository.delete(token);
    }
    
}