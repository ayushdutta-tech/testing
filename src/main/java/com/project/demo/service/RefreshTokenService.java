package com.project.demo.service;

import com.project.demo.model.RefreshToken;
import com.project.demo.repository.RefreshTokenRepository;
import com.project.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final long refreshTokenExpirationMs;

    /**
     * Primary constructor used by Spring (value injected).
     */
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            @Value("${jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Secondary constructor used by unit tests / Mockito (@InjectMocks).
     * Provides a sensible default expiration so Mockito can call this constructor
     * when it cannot resolve the @Value primitive.
     */
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository) {
        this(refreshTokenRepository, userRepository, 24L * 60L * 60L * 1000L); // default 24 hours
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String username) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        // Some repository implementations return the saved entity, some return void.
        // To be robust, call save(...) and then return our local object.
        try {
            refreshTokenRepository.save(refreshToken);
        } catch (Exception ex) {
            // If repository.save threw because of misconfiguration, rethrow a clearer message
            throw new RuntimeException("Failed to persist refresh token", ex);
        }
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.deleteByToken(token.getToken());
            throw new RuntimeException("Refresh token was expired. Please make a new sign-in request.");
        }
        return token;
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
