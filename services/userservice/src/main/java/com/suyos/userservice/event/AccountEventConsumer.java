package com.suyos.userservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdatedEvent;
import com.suyos.common.event.AccountUsernameUpdatedEvent;
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

    private final UserService userService;

    /**
     * Handles email updated event.
     */
    @KafkaListener(topics = "account-email-updated", groupId = "${spring.kafka.consumer.group-id}")
    public void handleEmailUpdated(AccountEmailUpdatedEvent event) {
        log.info("Received email updated event for account: {}", event.getAccountId());
        userService.mirrorEmailUpdate(event.getAccountId(), event.getNewEmail());
    }

    /**
     * Handles username updated event.
     */
    @KafkaListener(topics = "account-username-updated", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUsernameUpdated(AccountUsernameUpdatedEvent event) {
        log.info("Received username updated event for account: {}", event.getAccountId());
        userService.mirrorUsernameUpdate(event.getAccountId(), event.getNewUsername());
    }
    
}
