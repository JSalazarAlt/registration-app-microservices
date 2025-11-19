package com.suyos.authservice.service;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.suyos.authservice.model.Account;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for JWT token generation, validation, and extraction operations.
 * 
 * <p>Handles JWT-related functionality such as token creation, validation,
 * and claims extraction. Uses HMAC-SHA256 algorithm for token signing.</p>
 * 
 * @author Joel Salazar
 */
@Service
@Slf4j
public class JwtService {

    /** JWT secret key */
    @Value("${jwt.secret}")
    private String secretKey;
    
    /** JWT token expiration time in milliseconds (15 minutes) */
    @Value("${jwt.expiration:900000}")
    private Long jwtExpiration;

    // ----------------------------------------------------------------
    // JWT TOKEN GENERATION
    // ----------------------------------------------------------------

    /**
     * Builds JWT token with specified claims and expiration.
     * 
     * @param account Authenticated account
     * @return Generated JWT token
     */
    public String generateToken(Account account) {
        Map<String, Object> claims = Map.of(
            "username", account.getUsername(),
            "email", account.getEmail(),
            "authorities", List.of("ROLE_" + account.getRole().name())
        );

        return Jwts.builder()
                .claims(claims)
                .subject(account.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    // ----------------------------------------------------------------
    // CLAIMS EXTRACTION
    // ----------------------------------------------------------------

    /**
     * Extracts subject from JWT token.
     * 
     * <p>Used internally for extracting account ID from Authorization header.
     * OAuth2 Resource Server handles all JWT validation.</p>
     * 
     * @param jwtToken JWT token
     * @return Subject from the token (i.e., account ID)
     * @throws JwtException If JWT token is invalid or expired
     */
    public String extractSubject(String jwtToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error extracting subject from JWT: {}", e.getMessage());
            throw new JwtException("Invalid JWT token", e);
        }
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    /**
     * Gets the signing key for JWT token operations.
     * 
     * @return Signing key
     */
    private javax.crypto.SecretKey getSignInKey() {
        try {
            byte[] keyBytes;
            try {
                keyBytes = Base64.getDecoder().decode(secretKey);
            } catch (IllegalArgumentException e) {
                keyBytes = secretKey.getBytes();
                log.warn("JWT secret is not base64 encoded. Consider using base64 encoding for better security.");
            }
            
            if (keyBytes.length < 32) {
                log.warn("JWT secret key is shorter than recommended 256 bits (32 bytes)");
            }
            
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception e) {
            log.error("Failed to create signing key: {}", e.getMessage());
            throw new RuntimeException("JWT configuration error", e);
        }
    }

    /**
     * Gets JWT token expiration time in seconds.
     * 
     * @return Expiration time in seconds
     */
    public Long getExpirationTime() {
        return jwtExpiration / 1000;
    }
    
}