package com.suyos.authservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an authentication token associated with an
 * {@link Account}.
 * 
 * <p>Maps to the <b>tokens</b> table. Tokens are uniquely associated
 * with an account and used to validate authentication requests.</p>
 */
@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_account", columnList = "account_id"),
    @Index(name = "idx_token_value", columnList = "value", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    
    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    /** Unique identifier */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    /** Token value */
    @Column(name = "value", nullable = false, unique = true, length = 512)
    private String value;

    /** Type (e.g., refresh, email verification) */
    @Column(name = "type", nullable = false)
    private TokenType type;

    // ----------------------------------------------------------------
    // TOKEN ROTATION
    // ----------------------------------------------------------------

    /** Root token ID for tracking token rotation chains */
    @Column(name = "root_token_id")
    private Token rootTokenId;

    /** Parent token ID for tracking token rotation chains */
    @Column(name = "parent_token_id")
    private Token parentTokenId;

    /** Flag indicating if token has been reused */
    @Builder.Default
    @Column(name = "reused", nullable = false)
    private Boolean reused = false;

    /** Flag indicating if token has been revoked */
    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    /** Timestamp of revocation */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    // ----------------------------------------------------------------
    // LIFECYCLE
    // ----------------------------------------------------------------

    /** Timestamp of issuance */
    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    /** Timestamp of expiration */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    /** Account associated with token */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Session associated with token */
    @Column(name = "session_id")
    private UUID sessionId;
    
}