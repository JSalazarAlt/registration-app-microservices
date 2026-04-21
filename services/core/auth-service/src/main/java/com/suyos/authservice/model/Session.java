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

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "last_ip_address")
    private String lastIpAddress;

    @Column(name = "location")
    private String location;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    // ----------------------------------------------------------------
    // AUDITORY AND LIFECYCLE
    // ----------------------------------------------------------------

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason")
    private SessionTerminationReason terminationReason;

    @Column(name = "terminated_at")
    private Instant terminatedAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
}