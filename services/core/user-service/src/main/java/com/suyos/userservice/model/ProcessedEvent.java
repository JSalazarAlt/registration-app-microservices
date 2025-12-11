package com.suyos.userservice.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a processed Kafka event.
 * 
 * <p>Maps to the <b>processed_events</b> table and stores the event's its ID
 * and timestamp.</p>
 */
@Entity
@Table(name = "processed_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent {

    /** Unique identifier */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /** Timestamp when event ocurred */
    @Column(name = "occurred_at")
    private Instant occurredAt;
    
}