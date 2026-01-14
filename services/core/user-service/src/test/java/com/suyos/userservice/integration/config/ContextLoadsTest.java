package com.suyos.userservice.integration.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring context load test.
 *
 * <p>Verifies that the application context loads successfully with
 * all required beans and configurations.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class ContextLoadsTest {
    
    /**
     * Tests that the Spring application context loads without errors.
     */
    @Test
    void contextLoads() {
    }
}
