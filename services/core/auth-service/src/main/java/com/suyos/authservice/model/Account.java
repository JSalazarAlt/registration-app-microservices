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

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // ----------------------------------------------------------------
    // CREDENTIALS
    // ----------------------------------------------------------------

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // ----------------------------------------------------------------
    // AUTHORITY
    // ----------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private AccountRole role;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    @Builder.Default
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(name = "locked", nullable = false)
    private Boolean locked = false;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

    @Builder.Default
    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword = false;

    @Column(name = "last_password_changed_at")
    private Instant lastPasswordChangedAt;

    // ----------------------------------------------------------------
    // LOGIN AND LOGOUT TRACKING
    // ----------------------------------------------------------------

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_logout_at")
    private Instant lastLogoutAt;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    // ----------------------------------------------------------------
    // OAUTH2 INTEGRATION
    // ----------------------------------------------------------------

    @Column(name = "oauth2_provider")
    private String oauth2Provider;

    @Column(name = "oauth2_provider_id")
    private String oauth2ProviderId;

    // ----------------------------------------------------------------
    // MULTI-FACTOR INTEGRATION
    // ----------------------------------------------------------------

    @Builder.Default
    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_enabled_at")
    private Instant mfaEnabledAt;
    
    // ----------------------------------------------------------------
    // AUDITORY AND LIFECYCLE
    // ----------------------------------------------------------------

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "soft_deleted_at")
    private Instant softDeletedAt;

    @Column(name = "reactivated_at")
    private Instant reactivatedAt;
    
}