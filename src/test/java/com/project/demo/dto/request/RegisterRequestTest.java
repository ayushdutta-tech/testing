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
 * Unit tests for RegisterRequest DTO validation.
 */
class RegisterRequestTest {

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
    void whenUsernameTooShort_thenValidationFails() {
        RegisterRequest req = new RegisterRequest("ab", "Valid@123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty(), "Expected violation for short username");
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().contains("between 3 and 20")),
                "Username should trigger size validation error"
        );
    }

    @Test
    void whenPasswordTooShort_thenValidationFails() {
        RegisterRequest req = new RegisterRequest("validUser", "123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty(), "Expected violation for short password");
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().contains("at least 8")),
                "Password should trigger min length validation error"
        );
    }

    @Test
    void whenPasswordDoesNotMatchPattern_thenValidationFails() {
        // Missing uppercase and special character
        RegisterRequest req = new RegisterRequest("validUser", "password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertFalse(violations.isEmpty(), "Expected violation for weak password");
        assertTrue(
                violations.stream().anyMatch(v -> v.getMessage().contains("Password must contain")),
                "Password should trigger pattern validation error"
        );
    }

    @Test
    void whenAllFieldsValid_thenValidationPasses() {
        RegisterRequest req = new RegisterRequest("validUser", "Valid@123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);

        assertTrue(violations.isEmpty(), "Expected no violations for valid RegisterRequest");
    }
}
