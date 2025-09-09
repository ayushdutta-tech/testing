package com.project.demo.service;

import com.project.demo.model.User;
import com.project.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // mark lenient so unused stubs in some tests do NOT trigger UnnecessaryStubbingException
        Mockito.lenient().when(passwordEncoder.encode(any())).thenAnswer(inv -> "ENC(" + inv.getArgument(0) + ")");
    }

    @Test
    void register_shouldSaveUser() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("plain");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = userService.register(user);

        // service implementation returns "User registered successfully!" (note the exclamation)
        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loadUserByUsername_shouldReturnUser_whenExists() {
        User user = new User();
        user.setUsername("bob");
        user.setPassword("ENC(pass)");

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        User found = (User) userService.loadUserByUsername("bob");

        assertNotNull(found);
        assertEquals("bob", found.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.loadUserByUsername("ghost"));
    }
}
