package com.suyos.authservice.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.suyos.authservice.model.SessionTerminationReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session information.
 * 
 * <p>Contains sessions's state, device and network information.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfoResponse {

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    /** Unique identifier */
    private UUID id;

    /** Unique identifier linking to account */
    private UUID accountId;

    // ----------------------------------------------------------------
    // STATE
    // ----------------------------------------------------------------

    /** Flag indicating if session is active */
    private Boolean active;

    /** Reason for session termination */
    private SessionTerminationReason terminationReason;

    /** Timestamp when session was terminated */
    private Instant terminatedAt;

    // ----------------------------------------------------------------
    // DEVICE & NETWORK
    // ----------------------------------------------------------------

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    private String userAgent;

    /** Client device name */
    private String deviceName;

    /** IP address used during session creation */
    private String ipAddress;

    /** Last known IP address */
    private String lastIpAddress;

    /** Timestamp of last successful authenticated request */
    private Instant lastAccessedAt;

    /** Geographical location of client device */
    private String location;

    // ----------------------------------------------------------------
    // AUDITORY
    // ----------------------------------------------------------------

    /** Timestamp when session was created */
    private Instant createdAt;

}