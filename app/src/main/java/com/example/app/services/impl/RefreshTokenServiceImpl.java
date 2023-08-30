package com.example.app.services.impl;


import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.example.app.database.entities.RefreshToken;
import com.example.app.database.entities.User;
import com.example.app.database.repositories.RefreshTokenRepository;
import com.example.app.database.repositories.UserRepository;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl {
    @Value("${app.security.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(User userDb) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userDb);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;

    }

    public RefreshToken createAppRefreshToken(Long userId) {
        User userDb = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId));
        return createRefreshToken(userDb);

    }

    public RefreshToken createWebRefreshToken(Long userId) {
        User userDb = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", userId));
        Optional<RefreshToken> refreshTokenOptional = this.refreshTokenRepository.findByUser(userDb);
        if (refreshTokenOptional.isEmpty() || tokenWasExpired(refreshTokenOptional.get())) {
            return createRefreshToken(userDb);
        } else {
            return refreshTokenOptional.get();
        }
    }

    public boolean tokenWasExpired(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            return true;
        }

        return false;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (tokenWasExpired(token)) {
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }


}
