package com.project.demo.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginRequest DTO validation.
 */
class LoginRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void whenUsernameAndPasswordBlank_thenValidationFails() {
        LoginRequest req = new LoginRequest();
        req.setUsername("");
        req.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty(), "Expected violations for blank username and password");

        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().equals("Username is required")),
                "Username should trigger validation error"
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().equals("Password is required")),
                "Password should trigger validation error"
        );
    }

    @Test
    void whenValidUsernameAndPassword_thenValidationPasses() {
        LoginRequest req = new LoginRequest("alice", "securePassword");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(req);

        assertTrue(violations.isEmpty(), "Expected no violations for valid request");
    }
}
