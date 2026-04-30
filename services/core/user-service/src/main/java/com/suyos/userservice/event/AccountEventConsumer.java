package com.suyos.userservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    private final UserService userService;

    private static final String USER_CREATION_TOPIC = "user-creation";

    private static final String ACCOUNT_USERNAME_UPDATE_TOPIC = "account-username-update";

    private static final String ACCOUNT_EMAIL_UPDATE_TOPIC = "account-email-update";

    /**
     * Handles user creation events from Auth Service.
     * 
     * @param event Event metadatada, account's information, and user's profile
     */
    @KafkaListener(topics = USER_CREATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserCreation(UserCreationEvent event) {
        // Log user creation event reception
        log.info("event=kafka_user_creation_received account_id={}", event.getAccountId());
        
        // Create user
        try {
            userService.createUser(event);
        } catch (Exception e) {
            log.error("event=user_creation_failed account_id={} error={}", event.getAccountId(), e);
            throw e;
        }
    }

    /**
     * Handles username update events from Auth Service.
     * 
     * @param event Event metadata, account ID of the user, and new username
     */
    @KafkaListener(topics = ACCOUNT_USERNAME_UPDATE_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleAccountUsernameUpdate(AccountUsernameUpdateEvent event) {
        // Log username update event reception
        log.info("event=kafka_username_update_event_reception account_id={}", event.getAccountId());
        
        // Update user's username
        try {
            userService.mirrorUsernameUpdate(event);
        } catch (Exception e) {
            log.error("Failed to process username update event for account_id={}", event.getAccountId(), e);
        }
    }

    /**
     * Handles email update events from Auth Service.
     * 
     * @param event Event metadata, account ID of the user, and new email
     */
    @KafkaListener(topics = ACCOUNT_EMAIL_UPDATE_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleAccountEmailUpdate(AccountEmailUpdateEvent event) {
        // Log email update event reception
        log.info("event=kafka_email_update_event_reception account_id={}", event.getAccountId());
        
        // Update user's email
        try {
            userService.mirrorEmailUpdate(event);
        } catch (Exception e) {
            log.error("event=Failed to process email update event for account_id={}", event.getAccountId(), e);
        }
    }
    
}