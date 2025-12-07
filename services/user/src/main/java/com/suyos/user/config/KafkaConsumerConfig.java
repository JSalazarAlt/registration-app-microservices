package com.suyos.user.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka consumer configuration for User Service.
 * 
 * <p>Configures Kafka consumer to listen to account events from topics.</p>
 */
@Configuration
public class KafkaConsumerConfig {

    /** */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /** */
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /** Kafka template for publishing messages to dead letter topic */
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Configures Kafka consumer factory with deserialization settings.
     * 
     * <p>Creates a consumer factory that deserializes message keys as strings
     * and message values as JSON objects for consuming account events.</p>
     * 
     * @return Configured consumer factory for Kafka message consumption
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        // Build consumer configuration map
        Map<String, Object> config = new HashMap<>();
        
        // Set Kafka broker connection details
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Set consumer group ID for load balancing
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Configure key deserialization (account IDs as strings)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Configure value deserialization (events as JSON)
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Allow deserialization of trusted event packages for security
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.suyos.common.event");
        
        // Return consumer configuration
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Creates Kafka listener container factory for concurrent message
     * processing.
     * 
     * <p>Provides a factory for creating listener containers that can process
     * multiple messages concurrently using the consumer factory.</p>
     * 
     * @return Kafka listener container factory for event consumption
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        // Create concurrent listener container factory
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
            
        // Set consumer factory for message consumption
        factory.setConsumerFactory(consumerFactory());

        // Configure dead letter publishing recoverer for error handling
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        // Configure error handler with dead letter publishing
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));
        
        // Set error handler for listener container factory
        factory.setCommonErrorHandler(errorHandler);
        
        // Return concurrent listener container factory
        return factory;
    }
    
}