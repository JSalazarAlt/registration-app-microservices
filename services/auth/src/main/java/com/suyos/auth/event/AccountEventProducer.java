package com.suyos.auth.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
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

    /**
     * Publishes user's creation event to Kafka topic.
     * 
     * @param event Account's and user's profile data
     */
    public void publishUserCreation(UserCreationEvent event) {
        // Log user's creation event publication for debugging and monitoring
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
        // Log account's username update event publication for debugging and monitoring
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
        // Log account's email update event publication for debugging and monitoring
        log.info("event=kafka_email_update_published account_id={}", event.getAccountId());

        // Send event to Kafka topic with account ID as key
        kafkaTemplate.send(ACCOUNT_EMAIL_UPDATE_TOPIC, event.getAccountId().toString(), event);
    }
    
}