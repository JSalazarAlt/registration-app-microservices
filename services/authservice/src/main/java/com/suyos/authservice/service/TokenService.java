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
 * Service for token management operations.
 *
 * <p>Handles token generation, refresh, and revocation for flows such 
 * as refresh token rotation, email verification, and password reset. 
 * Uses {@link JwtService} for JWT access token operations.</p>
 * 
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {
    
    /** Repository for token data access operations */
    private final TokenRepository tokenRepository;

    /** Service for JWT token management */
    private final JwtService jwtService;

    // ----------------------------------------------------------------
    // TOKEN GENERATION
    // ----------------------------------------------------------------

    /**
     * Issues a new token of a specific type.
     * 
     * <p>Creates a new token of a specific type and associates it with an 
     * account. Used to issue tokens for email verification, password reset,
     * and other flows.</p>
     * 
     * @param account Account to associate the token with
     * @param type Type of token to issue
     * @param lifetimeInHours Duration in hours the token remains valid
     * @return Created token
     */
    public Token issueToken(Account account, TokenType type, Long lifetimeInHours) {
        // Generate a new token value
        String value = UUID.randomUUID().toString();

        // Create token
        Token token = new Token();
        token.setValue(value);
        token.setType(type);
        token.setAccount(account);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(lifetimeInHours));

        // Persist created token
        Token createdToken = tokenRepository.save(token);

        // Return created token
        return createdToken;
    }

    /**
     * Issues new refresh and access tokens for an authenticated account.
     * 
     * @param account Authenticated account
     * @return Refresh and access tokens
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

        // Build authentication response with refresh and access tokens
        AuthenticationResponseDTO response = AuthenticationResponseDTO.builder()
                .accountId(account.getId())
                .accessToken(accessToken)
                .refreshToken(value)
                .accessTokenExpiresIn(jwtService.getExpirationTime())
                .build();

        // Return refresh and access tokens
        return response;
    }

    // ----------------------------------------------------------------
    // TOKEN VALIDATION
    // ----------------------------------------------------------------

    /**
     * Checks if a token is valid (not revoked and not expired).
     * 
     * @param token Token to validate
     * @return True if token is valid, false otherwise
     */
    public boolean isTokenValid(Token token) {
        // Return true if token is not revoked and not expired
        return !token.getRevoked() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    /**
     * Refreshes access token using a valid refresh token (token rotation).
     * 
     * @param request Current refresh token value
     * @return Rotated refresh and access tokens
     * @throws RuntimeException If refresh token is invalid
     */
    public AuthenticationResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        // Extract refresh token value from request
        String value = request.getValue();
        
        // Lookup refresh token by value
        Token refreshToken = tokenRepository.findByValueAndType(value, TokenType.REFRESH)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        // Check if refresh token is invalid
        if (!isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Revoke old token for security
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        // Persist revoked token
        tokenRepository.save(refreshToken);

        // Build authentication response with new refresh and access tokens 
        // (refresh token rotation)
        AuthenticationResponseDTO response = issueRefreshAndAccessTokens(refreshToken.getAccount());

        // Return new refresh and access tokens
        return response;
    }

    // ----------------------------------------------------------------
    // TOKEN LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds a token by value.
     * 
     * @param value Token value to search for
     * @return Token
     * @throws RuntimeException If token is not found or invalid
     */
    public Token findTokenByValue(String value) {
        // Lookup token by value
        Token token = tokenRepository.findByValue(value)
            .orElseThrow(() -> new RuntimeException("Token not found"));
        
        // Return token
        return token;
    }

    /**
     * Finds a token by value and type.
     * 
     * @param value Token value to search for
     * @param type Token type to search for
     * @return Token
     * @throws RuntimeException If token is not found or invalid
     */
    public Token findTokenByValueAndType(String value, TokenType type) {
        // Lookup token by value and type
        Token token = tokenRepository.findByValueAndType(value, type)
            .orElseThrow(() -> new RuntimeException("Token not found"));

        // Return token
        return token;
    }

    // ----------------------------------------------------------------
    // TOKEN REVOCATION
    // ----------------------------------------------------------------

    /**
     * Revokes a token by value.
     * 
     * @param value Token value to revoke
     * @throws RuntimeException If token is invalid
     */
    public void revokeTokenByValue(String value) {
        // Lookup token by value
        Token refreshToken = tokenRepository.findByValue(value)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        // Mark token as revoked with timestamp
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        // Persist revoked token
        tokenRepository.save(refreshToken);
    }

    /**
     * Revokes all valid tokens by account ID.
     * 
     * <p>Used for password changes and account deletion flows.</p>
     * 
     * @param accountId Account ID
     */
    public void revokeAllTokensByAccountId(UUID accountId) {
        tokenRepository.revokeAllValidByAccountId(accountId);
    }

    /**
     * Revokes all valid tokens by account ID and type.
     * 
     * <p>Used for email verification resend and password reset flows.</p>
     * 
     * @param accountId Account ID
     * @param type Token type to revoke
     */
    public void revokeAllTokensByAccountIdAndType(UUID accountId, TokenType type) {
        tokenRepository.revokeAllValidByAccountIdAndType(accountId, type);
    }

    // ----------------------------------------------------------------
    // TOKEN DELETION
    // ----------------------------------------------------------------

    /**
     * Deletes a token.
     * 
     * @param value Token value to delete
     * @throws RuntimeException If token is invalid
     */
    public void deleteToken(String value) {
        // Lookup token by value
        Token token = tokenRepository.findByValue(value)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        // Delete token
        tokenRepository.delete(token);
    }
    
}