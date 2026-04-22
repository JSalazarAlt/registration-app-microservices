package com.suyos.authservice.dto.internal;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for session creation requests.
 * 
 * <p>Contains the device and network information, session expiration time,
 * and associated account identifier.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class SessionCreationRequest {

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    private final String userAgent;

    /** Device name (e.g., iPhone 12, Dell XPS 13) */
    private final String deviceName;

    /** IP address used during session creation */
    private final String firstIpAddress;

    private final String location;

    // ----------------------------------------------------------------
    // AUDITORY AND LIFECYCLE
    // ----------------------------------------------------------------

    private final Instant expiresAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    private final UUID accountId;
    
}