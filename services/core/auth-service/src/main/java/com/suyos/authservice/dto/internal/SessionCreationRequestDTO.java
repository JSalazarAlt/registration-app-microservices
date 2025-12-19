package com.suyos.authservice.dto.internal;

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
public class SessionCreationRequestDTO {

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    /** Unique identifier linking to account */
    private UUID accountId;

    // ----------------------------------------------------------------
    // SESSION'S INFORMATION
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
    
}