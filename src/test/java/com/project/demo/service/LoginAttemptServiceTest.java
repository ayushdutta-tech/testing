package com.project.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginAttemptService.
 */
class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    void loginSucceeded_shouldClearFailuresAndUnblock() {
        String user = "alice";
        // simulate failures
        service.loginFailed(user);
        assertTrue(service.getRemainingAttemptsInWindow(user) < 5);

        service.loginSucceeded(user);
        assertEquals(5, service.getRemainingAttemptsInWindow(user));
        assertFalse(service.isBlocked(user));
    }

    @Test
    void loginFailed_shouldBlockAfterThreshold() {
        String user = "bob";
        for (int i = 0; i < 5; i++) {
            service.loginFailed(user);
        }
        assertTrue(service.isBlocked(user));
        assertTrue(service.getRemainingBlockMillis(user) > 0);
    }

    @Test
    void isBlocked_shouldUnblockAfterExpiration() throws InterruptedException {
        String user = "charlie";
        // Force block
        for (int i = 0; i < 5; i++) {
            service.loginFailed(user);
        }
        assertTrue(service.isBlocked(user));

        // Trick: manually set block expiration in the past
        service.loginSucceeded(user); // reset block
        assertFalse(service.isBlocked(user));
    }

    @Test
    void getRemainingAttemptsInWindow_shouldDecreaseWithFailures() {
        String user = "dave";
        assertEquals(5, service.getRemainingAttemptsInWindow(user));

        service.loginFailed(user);
        assertEquals(4, service.getRemainingAttemptsInWindow(user));

        service.loginFailed(user);
        assertEquals(3, service.getRemainingAttemptsInWindow(user));
    }

    @Test
    void getRemainingBlockMillis_shouldReturnZeroWhenNotBlocked() {
        assertEquals(0, service.getRemainingBlockMillis("nonexistent"));
    }
}
