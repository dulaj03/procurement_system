package com.procure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * Adds a token to the blacklist with remaining TTL so it expires automatically from Redis
     */
    public void blacklistToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            // Calculate remaining token life
            long expirationTime = jwtService.extractClaim(token, claims -> claims.getExpiration().getTime());
            long remainingMs = expirationTime - System.currentTimeMillis();

            if (remainingMs > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, username, remainingMs, TimeUnit.MILLISECONDS);
                log.info("Token blacklisted successfully for user: {}. Remaining life: {} ms", username, remainingMs);
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.error("Redis lookup failed for blacklisted token check: {}", e.getMessage());
            return false;
        }
    }
}
