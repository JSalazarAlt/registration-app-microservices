package com.suyos.authservice.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.SessionCreationEvent;
import com.suyos.common.event.SessionTerminationEvent;
import com.suyos.common.event.UserCreationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka producer for account events.
 * 
 * <p>Publishes account-related events to Kafka topics for consumption by
 * other microservices.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventProducer {

    /** Kafka template for publishing events */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Kafka topic for user creation events */
    private static final String USER_CREATION_TOPIC = "user-creation";

    /** Kafka topic for account username update events */
    private static final String ACCOUNT_USERNAME_UPDATE_TOPIC = "account-username-update";
    
    /** Kafka topic for account email update events */
    private static final String ACCOUNT_EMAIL_UPDATE_TOPIC = "account-email-update";

    /** Kafka topic for session creation events */
    private static final String SESSION_CREATION_TOPIC = "session-creation";
    
    /** Kafka topic for session termination events */
    private static final String SESSION_TERMINATION_TOPIC = "session-termination";

    /**
     * Publishes user's creation event to Kafka topic.
     * 
     * @param event Account's information and user's profile
     */
    public void publishUserCreation(UserCreationEvent event) {
        // Log user's creation event publication
        log.info("event=kafka_user_creation_published account_id={}", event.getAccountId());

        // Send event to Kafka topic with account ID as key
        kafkaTemplate.send(USER_CREATION_TOPIC, event.getAccountId().toString(), event);
    }

    /**
     * Publishes account's username update event to Kafka topic.
     * 
     * @param event Account's ID and new username
     */
    public void publishAccountUsernameUpdate(AccountUsernameUpdateEvent event) {
        // Log account's username update event publication
        log.info("event=kafka_username_update_published account_id={}", event.getAccountId());

        // Send event to Kafka topic with account ID as key
        kafkaTemplate.send(ACCOUNT_USERNAME_UPDATE_TOPIC, event.getAccountId().toString(), event);
    }

    /**
     * Publishes account's email update event to Kafka topic.
     * 
     * @param event Account's ID and new email
     */
    public void publishAccountEmailUpdate(AccountEmailUpdateEvent event) {
        // Log account's email update event publication
        log.info("event=kafka_email_update_published account_id={}", event.getAccountId());

        // Send event to Kafka topic with account ID as key
        kafkaTemplate.send(ACCOUNT_EMAIL_UPDATE_TOPIC, event.getAccountId().toString(), event);
    }
    
    /**
     * Publishes sessions's creation event to Kafka topic.
     * 
     * @param event Session's information
     */
    public void publishSessionCreation(SessionCreationEvent event) {
        // Log sessions's creation event publication
        log.info("event=kafka_session_creation_published account_id={}", event.getAccountId());

        // Send event to Kafka topic with account ID as key
        kafkaTemplate.send(SESSION_CREATION_TOPIC, event.getAccountId().toString(), event);
    }

    /**
     * Publishes sessions's termination event to Kafka topic.
     * 
     * @param event Session's information
     */
    public void publishSessionTermination(SessionTerminationEvent event) {
        // Log sessions's creation event publication
        log.info("event=kafka_session_termination_published account_id={}", event.getAccountId());

        // Send event to Kafka topic with account ID as key
        kafkaTemplate.send(SESSION_TERMINATION_TOPIC, event.getAccountId().toString(), event);
    }

}