package com.suyos.userservice.integration.kafka;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.repository.UserRepository;

/**
 * Integration tests for Kafka event consumers.
 *
 * <p>Tests Kafka message consumption with Testcontainers to verify
 * event processing and database persistence.</p>
 */
@SpringBootTest
@Testcontainers
class KafkaConsumerIntegrationTest {
    
    /** Kafka container for integration testing */
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
    
    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    /** Kafka template for sending test messages */
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /** User repository for verifying persistence */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Tests user creation event consumption.
     */
    @Test
    void shouldConsumeUserCreationEvent() {
        UUID accountId = UUID.randomUUID();
        UserCreationEvent event = UserCreationEvent.builder()
            .id(UUID.randomUUID().toString())
            .occurredAt(Instant.now())
            .accountId(accountId)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        kafkaTemplate.send("user-creation", event);
        
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                assertTrue(userRepository.findByAccountId(accountId).isPresent());
            });
    }
    
    /**
     * Tests email update event consumption.
     */
    @Test
    void shouldConsumeEmailUpdateEvent() {
        UUID accountId = UUID.randomUUID();
        UserCreationEvent createEvent = UserCreationEvent.builder()
            .id(UUID.randomUUID().toString())
            .occurredAt(Instant.now())
            .accountId(accountId)
            .username("testuser")
            .email("old@example.com")
            .build();
        kafkaTemplate.send("user-creation", createEvent);
        
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertTrue(userRepository.findByAccountId(accountId).isPresent()));
        
        AccountEmailUpdateEvent updateEvent = new AccountEmailUpdateEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            accountId,
            "new@example.com"
        );
        kafkaTemplate.send("account-email-update", updateEvent);
        
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                var user = userRepository.findByAccountId(accountId).orElseThrow();
                assertEquals("new@example.com", user.getEmail());
            });
    }
    
    /**
     * Tests username update event consumption.
     */
    @Test
    void shouldConsumeUsernameUpdateEvent() {
        UUID accountId = UUID.randomUUID();
        UserCreationEvent createEvent = UserCreationEvent.builder()
            .id(UUID.randomUUID().toString())
            .occurredAt(Instant.now())
            .accountId(accountId)
            .username("oldusername")
            .email("test@example.com")
            .build();
        kafkaTemplate.send("user-creation", createEvent);
        
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertTrue(userRepository.findByAccountId(accountId).isPresent()));
        
        AccountUsernameUpdateEvent updateEvent = new AccountUsernameUpdateEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            accountId,
            "newusername"
        );
        kafkaTemplate.send("account-username-update", updateEvent);
        
        await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                var user = userRepository.findByAccountId(accountId).orElseThrow();
                assertEquals("newusername", user.getUsername());
            });
    }
}