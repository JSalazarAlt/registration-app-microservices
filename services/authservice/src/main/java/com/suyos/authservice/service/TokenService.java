package com.suyos.authservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.exception.exceptions.InvalidTokenException;
import com.suyos.authservice.exception.exceptions.TokenNotFoundException;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Token;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.TokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for token management operations.
 *
 * <p>Handles token generation, refresh, and revocation for flows such 
 * as refresh token rotation, email verification, and password reset. 
 * Uses {@link JwtService} for JWT access token operations.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
        token.setIssuedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(lifetimeInHours * 3600));

        // Persist created token
        Token createdToken = tokenRepository.save(token);

        // Log token issuance
        log.debug("event=token_issued type={} id={}", type, account.getId());

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
        refreshToken.setIssuedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(30 * 24 * 3600));

        // Persist created refresh token
        tokenRepository.save(refreshToken);

        // Log refresh token issuance event
        log.debug("event=token_issued type=REFRESH account_id={}", account.getId());

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
        return !token.getRevoked() && token.getExpiresAt().isAfter(Instant.now());
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    /**
     * Refreshes access token using a valid refresh token (token rotation).
     * 
     * @param request Refresh token value linked to account
     * @return Rotated refresh and access tokens
     * @throws InvalidTokenException If refresh token is invalid
     */
    public AuthenticationResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        // Extract refresh token value from request
        String value = request.getValue();
        
        // Lookup refresh token by value
        Token refreshToken = tokenRepository.findByValueAndType(value, TokenType.REFRESH)
                .orElseThrow(() -> new TokenNotFoundException(value));
        
        // Check if refresh token is invalid
        if (!isTokenValid(refreshToken)) {
            throw new InvalidTokenException(TokenType.REFRESH);
        }

        // Revoke old token for security
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());

        // Persist revoked token
        tokenRepository.save(refreshToken);

        // Log token rotation event
        log.info("event=refresh_token_rotated account_id={}", refreshToken.getAccount().getId());

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
            .orElseThrow(() -> new TokenNotFoundException(value));
        
        // Return token
        return token;
    }

    /**
     * Finds a token by value and type.
     * 
     * @param value Token value to search for
     * @param type Type of token to search for
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
        refreshToken.setRevokedAt(Instant.now());

        // Persist revoked token
        tokenRepository.save(refreshToken);

        // Log token revocation
        log.debug("event=token_revoked type={} value={}", refreshToken.getType(), refreshToken.getValue());
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

        // Log all tokens revocation
        log.info("event=all_tokens_revoked account_id={}", accountId);
    }

    /**
     * Revokes all valid tokens by account ID and type.
     * 
     * <p>Used for email verification resend and password reset flows.</p>
     * 
     * @param accountId Account ID
     * @param type Type of token to revoke
     */
    public void revokeAllTokensByAccountIdAndType(UUID accountId, TokenType type) {
        tokenRepository.revokeAllValidByAccountIdAndType(accountId, type);

        // Log tokens revocation
        log.debug("event=all_tokens_revoked type={} account_id={}", type, accountId);
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