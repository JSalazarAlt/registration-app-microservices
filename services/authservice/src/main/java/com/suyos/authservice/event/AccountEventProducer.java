package com.suyos.authservice.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdatedEvent;
import com.suyos.common.event.AccountUsernameUpdatedEvent;

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

    private static final String EMAIL_UPDATED_TOPIC = "account-email-updated";
    private static final String USERNAME_UPDATED_TOPIC = "account-username-updated";

    /**
     * Publishes email updated event.
     */
    public void publishEmailUpdated(AccountEmailUpdatedEvent event) {
        log.info("Publishing email updated event for account: {}", event.getAccountId());
        kafkaTemplate.send(EMAIL_UPDATED_TOPIC, event.getAccountId().toString(), event);
    }

    /**
     * Publishes username updated event.
     */
    public void publishUsernameUpdated(AccountUsernameUpdatedEvent event) {
        log.info("Publishing username updated event for account: {}", event.getAccountId());
        kafkaTemplate.send(USERNAME_UPDATED_TOPIC, event.getAccountId().toString(), event);
    }
    
}
