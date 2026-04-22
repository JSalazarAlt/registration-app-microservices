package com.suyos.authservice.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.suyos.authservice.model.SessionTerminationReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for session information.
 * 
 * <p>Contains the session status, device and network information, session
 * lifecycle information, and associated account identifier.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class SessionResponse {

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    private final UUID id;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    private final Boolean active;

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    private final String userAgent;

    /** Device name (e.g., iPhone 12, Dell XPS 13) */
    private final String deviceName;

    /** IP address used during session creation */
    private final String firstIpAddress;

    /** Most recent IP address observed during session activity */
    private final String lastIpAddress;

    private final String location;

    private final Instant lastActivityAt;

    // ----------------------------------------------------------------
    // AUDITORY AND LIFECYCLE
    // ----------------------------------------------------------------

    private final Instant createdAt;

    private final SessionTerminationReason terminationReason;

    private final Instant terminatedAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------
   
    private final UUID accountId;

}