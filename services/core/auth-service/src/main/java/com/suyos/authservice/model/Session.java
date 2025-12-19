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
 * Entity representing an authentication session.
 *
 * <p>Maps to the <b>sessions</b> table and stores refresh token metadata,
 * device information, and session lifecycle state.</p>
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_account_id", columnList = "account_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    /** Unique identifier */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    /** Unique identifier linking to account */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    // ----------------------------------------------------------------
    // STATE
    // ----------------------------------------------------------------

    /** Flag indicating if session is active */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /** Timestamp when session expires */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Reason for session termination */
    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason")
    private SessionTerminationReason terminationReason;

    /** Timestamp when session was terminated */
    @Column(name = "terminated_at")
    private Instant terminatedAt;

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    @Column(name = "user_agent")
    private String userAgent;

    /** Client device name */
    @Column(name = "device_name")
    private String deviceName;

    /** IP address used during session creation */
    @Column(name = "ip_address")
    private String ipAddress;

    /** Last known IP address */
    @Column(name = "last_ip_address")
    private String lastIpAddress;

    // ----------------------------------------------------------------
    // AUDITORY
    // ----------------------------------------------------------------

    /** Timestamp when session was created */
    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    /** Timestamp when session was last updated */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
}