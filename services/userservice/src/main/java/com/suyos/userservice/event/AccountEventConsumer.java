package com.suyos.userservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserCreationRequestDTO;
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
     * Handles user creation event.
     */
    @KafkaListener(topics = "user-creation", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserCreation(UserCreationEvent event) {
        log.info("Received creation event for user: {}", event.getAccountId());
        
        UserCreationRequestDTO request = UserCreationRequestDTO.builder()
            .accountId(event.getAccountId())
            .username(event.getUsername())
            .email(event.getEmail())
            .firstName(event.getFirstName())
            .lastName(event.getLastName())
            .phone(event.getPhone())
            .profilePictureUrl(event.getProfilePictureUrl())
            .locale(event.getLocale())
            .timezone(event.getTimezone())
            .build();
            
        userService.createUser(request);
    }

    /**
     * Handles account's  username update event.
     */
    @KafkaListener(topics = "account-username-update", groupId = "${spring.kafka.consumer.group-id}")
    public void handleAccountUsernameUpdate(AccountUsernameUpdateEvent event) {
        log.info("Received username updated event for account: {}", event.getAccountId());
        userService.mirrorUsernameUpdate(event.getAccountId(), event.getNewUsername());
    }

    /**
     * Handles account's email update event.
     */
    @KafkaListener(topics = "account-email-update", groupId = "${spring.kafka.consumer.group-id}")
    public void handleAccountEmailUpdate(AccountEmailUpdateEvent event) {
        log.info("Received email updated event for account: {}", event.getAccountId());
        userService.mirrorEmailUpdate(event.getAccountId(), event.getNewEmail());
    }
    
}
