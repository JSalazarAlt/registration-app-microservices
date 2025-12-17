package com.suyos.sessionservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.suyos.common.event.GlobalSessionTerminationEvent;
import com.suyos.common.event.SessionCreationEvent;
import com.suyos.common.event.SessionTerminationEvent;
import com.suyos.sessionservice.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for account events.
 * 
 * <p>Listens to account-related events to create and terminate sessions.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    /** Service for session business logic */
    private final SessionService sessionService;

    /** Kafka topic for session creation events */
    private static final String SESSION_CREATION_TOPIC = "session-creation";

    /** Kafka topic for session termination events */
    private static final String SESSION_TERMINATION_TOPIC = "session-termination";

    /** Kafka topic for global session termination events */
    private static final String GLOBAL_SESSION_TERMINATION_TOPIC = "global-session-termination";

    /**
     * Handles session creation event from Auth Service.
     * 
     * @param event Event's metadata and session's information
     */
    @KafkaListener(topics = SESSION_CREATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSessionCreation(SessionCreationEvent event) {
        // Log session creation event reception
        log.info("event=kafka_session_creation_received account_id={}", event.getAccountId());
        
        // Create session
        try {
            sessionService.createSession(event);
        } catch (Exception e) {
            log.error("event=session_creation_failed account_id={} error={}", event.getAccountId(), e);
            throw e;
        }
    }

    /**
     * Handles session termination event from Auth Service.
     * 
     * @param event Event's metadata and session's 
     */
    @KafkaListener(topics = SESSION_TERMINATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSessionTermination(SessionTerminationEvent event) {
        // Log account's session termination event reception
        log.info("event=kafka_session_termination_received account_id={}", event.getAccountId());
        
        // Terminate session
        try {
            sessionService.terminateSession(event);
        } catch (Exception e) {
            log.error("event=session_termination_failed account_id={}", event.getAccountId(), e);
        }
    }

    /**
     * Handles global session termination event from Auth Service.
     * 
     * @param event Event's metadata and session's 
     */
    @KafkaListener(topics = GLOBAL_SESSION_TERMINATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleGlobalSessionTermination(GlobalSessionTerminationEvent event) {
        // Log account's global session termination event reception
        log.info("event=kafka_global_session_termination_received account_id={}", event.getAccountId());
        
        // Terminate all sessions for account
        try {
            sessionService.terminateAllSessions(event);
        } catch (Exception e) {
            log.error("event=global_session_termination_failed account_id={}", event.getAccountId(), e);
        }
    }
    
}