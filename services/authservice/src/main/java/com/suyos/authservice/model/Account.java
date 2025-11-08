package com.suyos.authservice.model;

import java.time.LocalDateTime;
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
 * Entity representing an account in the authentication system.
 * 
 * <p>Maps to the <b>accounts</b> table and contains all fields for account
 * management and authentication.</p>
 * 
 * @author Joel Salazar
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

    /** Unique identifier for the account record */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // IDENTITY

    /** User's chosen username */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** User's email address used for login and communication */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Flag indicating if the user's email address has been verified */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /** Encrypted password hash for user authentication */
    @Column(name = "password", nullable = false)
    private String password;

    // AUTHORIZATION

    /** Encrypted password hash for user authentication */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // STATUS

    /** Flag indicating if the user account is enabled */
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /** Flag indicating if the user account is temporarily locked */
    @Builder.Default
    @Column(name = "locked", nullable = false)
    private Boolean locked = false;

    /** Timestamp when the account lock expires */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /** Flag indicating if the user account was deactivated */
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // PASSWORD MANAGEMENT

    /** Flag indicating if user must change password on next login */
    @Builder.Default
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    /** Timestamp when the user's password was last changed */
    @Column(name = "last_password_changed_at")
    private LocalDateTime lastPasswordChangedAt;

    // LOGIN AND LOGOUT TRACKING

    /** Timestamp of the user's last successful login */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** Timestamp of the user's last successful login */
    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    /** Counter for consecutive failed login attempts */
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    // OAUTH2 INTEGRATION

    /** OAuth2 provider name (google) - null for traditional login */
    @Column(name = "oauth2_provider")
    private String oauth2Provider;

    /** Unique identifier from OAuth2 provider - null for traditional login */
    @Column(name = "oauth2_provider_id")
    private String oauth2ProviderId;

    // MULTI-FACTOR INTEGRATION

    /** Flag indicating if multi-factor authentication is enabled */
    @Builder.Default
    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    /** Timestamp when multi-factor authentication was last enabled */
    @Column(name = "mfa_enabled_at")
    private LocalDateTime mfaEnabledAt;
    
    // LIFECYCLE

    /** Timestamp when the account record was first created in the system */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the account record was last modified */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Timestamp when the account record was deleted */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
}