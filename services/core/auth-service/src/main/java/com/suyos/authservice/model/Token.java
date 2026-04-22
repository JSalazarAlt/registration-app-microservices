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
 * Entity representing an authentication token.
 * 
 * <p>Maps to the <b>tokens</b> table. Contains fields for token rotation,
 * and associated account and session.</p>
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

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    /** Unique token value represented as UUID string */
    @Column(name = "value", nullable = false, unique = true, length = 512)
    private String value;

    @Column(name = "type", nullable = false)
    private TokenType type;

    // ----------------------------------------------------------------
    // ROTATION
    // ----------------------------------------------------------------

    /** Root token ID for tracking token rotation chains */
    @Column(name = "root_token_id")
    private Token rootTokenId;

    /** Parent token ID for tracking token rotation chains */
    @Column(name = "parent_token_id")
    private Token parentTokenId;

    @Builder.Default
    @Column(name = "reused", nullable = false)
    private Boolean reused = false;

    // ----------------------------------------------------------------
    // AUDITORY AND LIFECYCLE
    // ----------------------------------------------------------------

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "session_id")
    private UUID sessionId;
    
}