package com.suyos.userservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for account events.
 * 
 * <p>Listens to account-related events and updates user profiles.</p>
 * 
 * @author Joel Salazar
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    /** Service for user business logic */
    private final UserService userService;

    /** Kafka topic for user creation events */
    private static final String USER_CREATION_TOPIC = "user-creation";

    /** Kafka topic for account username update events */
    private static final String ACCOUNT_USERNAME_UPDATE_TOPIC = "account-username-update";

    /** Kafka topic for account email update events */
    private static final String ACCOUNT_EMAIL_UPDATE_TOPIC = "account-email-update";

    /**
     * Handles user creation event from Auth Service.
     * 
     * @param event User creation event containing account and profile data
     */
    @KafkaListener(topics = USER_CREATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserCreation(UserCreationEvent event) {
        try {
            // Log user creation event reception for debugging and monitoring
            log.info("Received creation event for user: {}", event.getAccountId());
                
            // Create user's profile
            userService.createUser(event);
        } catch (Exception e) {
            log.error("Failed to process user creation event for account: {}", event.getAccountId(), e);
        }
    }

    /**
     * Handles account username update event from Auth Service.
     * 
     * @param event Username update event containing account ID and new username
     */
    @KafkaListener(topics = ACCOUNT_USERNAME_UPDATE_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleAccountUsernameUpdate(AccountUsernameUpdateEvent event) {
        try {
            // Log account's username update event reception for debugging and monitoring
            log.info("Received username updated event for account: {}", event.getAccountId());
            
            // Update user's username
            userService.mirrorUsernameUpdate(event);
        } catch (Exception e) {
            log.error("Failed to process username update event for account: {}", event.getAccountId(), e);
        }
    }

    /**
     * Handles account email update event from Auth Service.
     * 
     * @param event Email update event containing account ID and new email
     */
    @KafkaListener(topics = ACCOUNT_EMAIL_UPDATE_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleAccountEmailUpdate(AccountEmailUpdateEvent event) {
        try {
            // Log account's email update event reception for debugging and monitoring
            log.info("Received email updated event for account: {}", event.getAccountId());
            
            // Update user's email
            userService.mirrorEmailUpdate(event);
        } catch (Exception e) {
            log.error("Failed to process email update event for account: {}", event.getAccountId(), e);
        }
    }
    
}