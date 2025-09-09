package com.project.demo.service;

import com.project.demo.model.RefreshToken;
import com.project.demo.model.User;
import com.project.demo.repository.RefreshTokenRepository;
import com.project.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock UserRepository userRepository;

    RefreshTokenService refreshTokenService;

    @BeforeEach
    void init() {
        // short expiry used for tests (ms)
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, 24 * 60 * 60 * 1000L);
    }

    @Test
    void createRefreshToken_shouldReturnToken_whenUserExists() {
        String username = "u1";
        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(new User()));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(username);

        assertNotNull(token);
        assertEquals(username, token.getUsername());
        assertNotNull(token.getToken());
        assertTrue(token.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> refreshTokenService.createRefreshToken("ghost"));
        assertTrue(ex.getMessage().toLowerCase().contains("user not found"));
    }

    @Test
    void findByToken_and_verifyExpiration_shouldWork() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("t1");
        rt.setUsername("u1");
        rt.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(eq("t1"))).thenReturn(Optional.of(rt));

        Optional<RefreshToken> found = refreshTokenService.findByToken("t1");
        assertTrue(found.isPresent());
        assertEquals("t1", found.get().getToken());

        RefreshToken verified = refreshTokenService.verifyExpiration(rt);
        assertSame(rt, verified);
    }

    @Test
    void verifyExpiration_shouldThrow_whenExpired() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("expired");
        rt.setUsername("u");
        rt.setExpiryDate(Instant.now().minusSeconds(60));

        doNothing().when(refreshTokenRepository).deleteByToken(eq("expired"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(rt));
        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        verify(refreshTokenRepository).deleteByToken("expired");
    }
}
