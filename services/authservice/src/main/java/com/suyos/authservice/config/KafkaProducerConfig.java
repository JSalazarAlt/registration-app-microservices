package com.suyos.authservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Kafka producer configuration for Auth Service.
 * 
 * <p>Configures Kafka producer to publish account events to topics.</p>
 */
@Configuration
public class KafkaProducerConfig {

    /** */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Configures Kafka producer factory with serialization settings.
     * 
     * <p>Creates a producer factory that serializes message keys as strings
     * and message values as JSON for publishing account events.</p>
     * 
     * @return Configured producer factory for Kafka message publishing
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        // Build producer configuration map
        Map<String, Object> config = new HashMap<>();

        // ----------------------------------------------------------------
        // SERIALIZATION SETTINGS
        // ----------------------------------------------------------------
        
        // Set Kafka broker connection details
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Configure key serialization (account IDs as strings)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Configure value serialization (events as JSON)
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // ----------------------------------------------------------------
        // RELIABILITY SETTINGS
        // ----------------------------------------------------------------

        // Wait until all in-sync replicas have acknowledge the message
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Avoid creating duplicate messages when retries occur
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Retry sending forever until Kafka becomes available
        config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        
        // Allow up to 5 Kafka request to be in-flight (send but not yet acknowledged)
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Return producer configuration
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Creates Kafka template for publishing events.
     * 
     * @return Kafka template for event publishing operations
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
}