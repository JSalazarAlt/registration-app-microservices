package com.suyos.common.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCreationEvent {

    // ----------------------------------------------------------------
    // EVENT METADATA
    // ----------------------------------------------------------------

    /** Unique identifier for the event */
    private String id;

    /** Timestamp when event ocurred */
    private Instant occurredAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    /** Unique identifier linking to account */
    private UUID accountId;

    // ----------------------------------------------------------------
    // SESSION'S INFORMATION
    // ----------------------------------------------------------------

    /** Unique identifier of session to be created */
    private UUID sessionId;

    /** Reported user agent (e.g., Chrome, Safari, Android, iOS) */
    private String userAgent;

    /** Client device name */
    private String deviceName;

    /** IP address used during session creation */
    private String ipAddress;

    /** Last known IP address */
    private String lastIpAddress;
    
}