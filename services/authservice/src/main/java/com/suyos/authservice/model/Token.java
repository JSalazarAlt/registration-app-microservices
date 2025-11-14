package com.suyos.authservice.model;

import java.time.LocalDateTime;
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
 * 
 * @author Joel Salazar
 */
@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_account", columnList = "account_id"),
    @Index(name = "idx_token_value", columnList = "token", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    /** Unique identifier for the token record */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // IDENTITY

    /** Token value for authentication */
    @Column(name = "value", nullable = false, unique = true, length = 512)
    private String value;

    /** Type of the token (e.g., refresh, email verification) */
    @Column(name = "type", nullable = false)
    private TokenType type;

    // TOKEN ROTATION

    /** Root token ID for tracking token rotation chains */
    @Column(name = "root_token_id")
    private UUID rootTokenId;

    /** Parent token ID for tracking token rotation chains */
    @Column(name = "parent_token_id")
    private UUID parentTokenId;

    /** Flag indicating if token has been reused */
    @Builder.Default
    @Column(name = "reused", nullable = false)
    private Boolean reused = false;

    /** Flag indicating if token has been revoked */
    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    /** Timestamp when the token was revoked */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    // RELATIONSHIPS

    /** Account associated with this token */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Session ID associated with this token */
    @JoinColumn(name = "session_id", nullable = false)
    private UUID sessionId;

    // LIFECYCLE

    /** Timestamp when the token was issued */
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    /** Timestamp when the token expires */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
}