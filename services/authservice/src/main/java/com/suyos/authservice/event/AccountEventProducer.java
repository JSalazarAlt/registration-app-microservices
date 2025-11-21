package com.suyos.authservice.event;

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
 * 
 * @author Joel Salazar
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_CREATION_TOPIC = "user-creation";
    private static final String ACCOUNT_EMAIL_UPDATE_TOPIC = "account-email-update";
    private static final String ACCOUNT_USERNAME_UPDATE_TOPIC = "account-username-update";

    /**
     * Publishes user creation event.
     */
    public void publishUserCreation(UserCreationEvent event) {
        log.info("Publishing creation event for user: {}", event.getAccountId());
        kafkaTemplate.send(USER_CREATION_TOPIC, event.getAccountId().toString(), event);
    }
    
    /**
     * Publishes account's email update event.
     */
    public void publishAccountEmailUpdate(AccountEmailUpdateEvent event) {
        log.info("Publishing email updated event for account: {}", event.getAccountId());
        kafkaTemplate.send(ACCOUNT_EMAIL_UPDATE_TOPIC, event.getAccountId().toString(), event);
    }

    /**
     * Publishes account's username update event.
     */
    public void publishAccountUsernameUpdate(AccountUsernameUpdateEvent event) {
        log.info("Publishing username updated event for account: {}", event.getAccountId());
        kafkaTemplate.send(ACCOUNT_USERNAME_UPDATE_TOPIC, event.getAccountId().toString(), event);
    }
    
}