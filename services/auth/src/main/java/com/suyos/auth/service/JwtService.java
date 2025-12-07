package com.suyos.auth.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.suyos.auth.model.Account;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for JWT token generation, validation, and extraction operations.
 * 
 * <p>Handles JWT-related functionality such as token creation, validation,
 * and claims extraction. Uses HMAC-SHA256 algorithm for token signing.</p>
 */
@Service
@Slf4j
public class JwtService {
    
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
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    /**
     * Gets JWT token expiration time in seconds.
     * 
     * @return Expiration time in seconds
     */
    public Long getExpirationTime() {
        return jwtExpiration / 1000;
    }

    /**
     * Gets the signing key for JWT token operations.
     * 
     * @return Signing key
     */
    private PrivateKey getPrivateKey() {
        try (var inputStream = getClass().getResourceAsStream("/keys/private.pem")) {
            var keyBytes = inputStream.readAllBytes();
            var kf = KeyFactory.getInstance("RSA");
            var spec = new PKCS8EncodedKeySpec(stripPemHeaders(keyBytes));
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private byte[] stripPemHeaders(byte[] key) {
        return Base64.getDecoder().decode(
            new String(key)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "")
        );
    }
    
}