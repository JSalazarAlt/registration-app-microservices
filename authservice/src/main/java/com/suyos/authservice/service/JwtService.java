package com.suyos.authservice.service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
 * <p>Handles all JWT-related functionality including token creation, validation,
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

    /**
     * Extracts username from JWT token.
     * 
     * @param token the JWT token
     * @return the username (subject) from the token
     * @throws JwtException if token is invalid or expired
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
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
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from JWT token.
     * 
     * @param <T> the type of the claim
     * @param token the JWT token
     * @param claimsResolver function to extract the specific claim
     * @return the extracted claim
     * @throws JwtException if token is invalid
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error extracting claim from JWT: {}", e.getMessage());
            throw new JwtException("Failed to extract claim from token", e);
        }
    }

    /**
     * Generates JWT token for user authentication.
     * 
     * @param userDetails the authenticated user details
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates JWT token with additional claims.
     * 
     * @param extraClaims additional claims to include in the token
     * @param userDetails the authenticated user details
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Gets JWT token expiration time in seconds.
     * 
     * @return expiration time in seconds
     */
    public Long getExpirationTime() {
        return jwtExpiration / 1000;
    }

    /**
     * Validates JWT token against user details.
     * 
     * @param token the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if JWT token is expired.
     * 
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts all claims from JWT token.
     * 
     * @param token the JWT token
     * @return all claims from the token
     * @throws JwtException if token parsing fails
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
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
     * Builds JWT token with specified claims and expiration.
     * 
     * @param extraClaims additional claims to include
     * @param userDetails the user details
     * @param expiration token expiration time in milliseconds
     * @return the built JWT token
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Gets the signing key for JWT token operations.
     * 
     * @return the signing key
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
    
}