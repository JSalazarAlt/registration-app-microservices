package com.suyos.authservice.service;

import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

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

    /** JWT secret key from application properties */
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
            "role", account.getRole().name()
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
     * Extracts all claims from JWT token.
     * 
     * @param jwtToken JWT token
     * @return All claims from the token
     * @throws JwtException If JWT token parsing fails
     */
    private Claims extractAllClaims(String jwtToken) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired");
            throw e;
        } catch (MalformedJwtException e) {
            log.debug("Malformed JWT token");
            throw e;
        } catch (SignatureException e) {
            log.debug("JWT signature validation failed");
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new JwtException("Token parsing failed", e);
        }
    }

    /**
     * Extracts a specific claim from JWT token.
     * 
     * @param <T> Type of the claim
     * @param jwtToken JWT token
     * @param claimsResolver Function to extract the specific claim
     * @return Extracted claim
     * @throws JwtException If JWT token is invalid
     */
    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(jwtToken);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error extracting claim from JWT: {}", e.getMessage());
            throw new JwtException("Failed to extract claim from token", e);
        }
    }

    /**
     * Extracts subject from JWT token.
     * 
     * @param jwtToken JWT token
     * @return Subject from the token (i.e., account ID)
     * @throws JwtException If JWT token is invalid or expired
     */
    public String extractSubject(String jwtToken) {
        try {
            return extractClaim(jwtToken, Claims::getSubject);
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
            log.error("Error extracting username from JWT: {}", e.getMessage());
            throw new JwtException("Invalid JWT token", e);
        }
    }

    /**
     * Extracts expiration date from JWT token.
     * 
     * @param jwtToken JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }

    /**
     * Validates JWT token against user details.
     * 
     * @param jwtToken JWT token to validate
     * @param expectedAccountId Expected account ID in the token
     * @return True if token is valid, false otherwise
     */
    public boolean isTokenValid(String jwtToken, String expectedAccountId) {
        try {
            return extractSubject(jwtToken).equals(expectedAccountId) && !isTokenExpired(jwtToken);
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------------------------------------------------------
    // JWT TOKEN VALIDATION
    // ----------------------------------------------------------------

    /**
     * Checks if JWT token is expired.
     * 
     * @param jwtToken JWT token
     * @return True if token is expired, false otherwise
     */
    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
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