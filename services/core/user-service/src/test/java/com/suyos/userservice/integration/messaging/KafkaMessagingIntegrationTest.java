package com.suyos.userservice.integration.messaging;

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

import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.repository.UserRepository;

/**
 * Integration tests for Kafka messaging.
 *
 * <p>Tests Kafka message consumption with Testcontainers to verify
 * event processing and database persistence.</p>
 */
@SpringBootTest
@Testcontainers
class KafkaMessagingIntegrationTest {
    
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
            .untilAsserted(() -> assertTrue(userRepository.findByAccountId(accountId).isPresent()));
    }
}
