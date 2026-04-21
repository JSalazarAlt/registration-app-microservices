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
public class SessionInfoResponse {

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    /** Unique identifier */
    private final UUID id;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    /** Flag indicating if session is active */
    private final Boolean active;

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    private final String userAgent;

    /** Client device name */
    private final String deviceName;

    /** IP address used during session creation */
    private final String ipAddress;

    /** Last known IP address */
    private final String lastIpAddress;

    /** Geographical location of client device */
    private final String location;

    /** Timestamp of last successful authenticated request */
    private final Instant lastActivityAt;

    // ----------------------------------------------------------------
    // AUDITORY AND LIFECYCLE
    // ----------------------------------------------------------------

    /** Timestamp when session was created */
    private final Instant createdAt;

    /** Reason for session termination */
    private final SessionTerminationReason terminationReason;

    /** Timestamp when session was terminated */
    private final Instant terminatedAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------
   
    /** Unique identifier linking to account */
    private final UUID accountId;

}