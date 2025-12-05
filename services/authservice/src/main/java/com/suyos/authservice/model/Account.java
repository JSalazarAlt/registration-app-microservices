package com.suyos.authservice.model;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an account.
 * 
 * <p>Maps to the <b>accounts</b> table and contains all fields for account
 * management and authentication.</p>
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_email", columnList = "email"),
    @Index(name = "idx_account_username", columnList = "username"),
    @Index(name = "idx_account_oauth2", columnList = "oauth2_provider, oauth2_provider_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    /** Unique identifier */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    /** Username */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** Email address */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Flag indicating if email address has been verified */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /** Encrypted password hash */
    @Column(name = "password", nullable = false)
    private String password;

    // ----------------------------------------------------------------
    // AUTHORITY
    // ----------------------------------------------------------------

    /** Role defining access level */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    /** Flag indicating if account is enabled */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /** Flag indicating if account is temporarily locked */
    @Builder.Default
    @Column(name = "locked", nullable = false)
    private Boolean locked = false;

    /** Timestamp when account lock expires */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    /** Flag indicating if account was soft deleted */
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /** Timestamp when account was soft deleted */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

    /** Flag indicating if password must be changed on next login */
    @Builder.Default
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    /** Timestamp when password was last changed */
    @Column(name = "last_password_changed_at")
    private Instant lastPasswordChangedAt;

    // ----------------------------------------------------------------
    // LOGIN AND LOGOUT TRACKING
    // ----------------------------------------------------------------

    /** Timestamp of last successful login */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /** Timestamp of last successful logout */
    @Column(name = "last_logout_at")
    private Instant lastLogoutAt;

    /** Counter for consecutive failed login attempts */
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    // ----------------------------------------------------------------
    // OAUTH2 INTEGRATION
    // ----------------------------------------------------------------

    /** OAuth2 provider name (e.g., Google, Facebook) */
    @Column(name = "oauth2_provider")
    private String oauth2Provider;

    /** Unique identifier from OAuth2 provider */
    @Column(name = "oauth2_provider_id")
    private String oauth2ProviderId;

    // ----------------------------------------------------------------
    // MULTI-FACTOR INTEGRATION
    // ----------------------------------------------------------------

    /** Flag indicating if multi-factor authentication is enabled */
    @Builder.Default
    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    /** Timestamp when multi-factor authentication was enabled */
    @Column(name = "mfa_enabled_at")
    private Instant mfaEnabledAt;
    
    // ----------------------------------------------------------------
    // AUDITORY
    // ----------------------------------------------------------------

    /** Timestamp when account record was first created */
    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    /** Timestamp when account record was last modified */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
}