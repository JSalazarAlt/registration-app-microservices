package com.suyos.authservice.dto.internal;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session creation requests.
 * 
 * <p>Contains sessions's device and network information and account's ID
 * linked to session.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCreationRequest {

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    /** Unique identifier linking to account */
    private UUID accountId;

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    /** Timestamp when session expires */
    private Instant expiresAt;

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    private String userAgent;

    /** Client device name */
    private String deviceName;

    /** IP address used during session creation */
    private String ipAddress;

    /** Last known IP address */
    private String lastIpAddress;

    /** Geographical location of client device */
    private String location;

    // ----------------------------------------------------------------
    // AUDITORY
    // ----------------------------------------------------------------

    /** Timestamp when session was created */
    private Instant createdAt;
    
}