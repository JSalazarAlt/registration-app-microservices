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
 * Entity representing an authentication or session token associated with an 
 * {@link Account}.
 * 
 * <p>This entity maps to the <b>tokens</b> table in the database and stores 
 * metadata for token lifecycle management (e.g., issue and expiration 
 * timestamps.</p>
 * 
 * <p>Tokens are uniquely associated with an account and used to validate 
 * authentication requests within the system.</p>
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    /** Timestamp when the user record was issued */
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    /** Timestamp when the token expires */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /** Timestamp when the token expires */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
}