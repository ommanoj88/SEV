package com.evfleet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic smoke test to verify the application context loads successfully.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class EvFleetApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        // It verifies that all beans are properly configured
    }

    @Test
    void applicationStarts() {
        // Verify the application can start
        // This is a basic smoke test
    }
}
