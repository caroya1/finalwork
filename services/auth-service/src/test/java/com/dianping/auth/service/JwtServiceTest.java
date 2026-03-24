package com.dianping.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private JwtService jwtService;
    private final String secret = "mySecretKeyForTestingPurposeOnly1234567890";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jwtService = new JwtService(secret, 120, 7, redisTemplate);
    }

    @Test
    void generateAccessToken_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";

        // When
        String token = jwtService.generateAccessToken(userId, username, role);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        Claims claims = jwtService.parseClaims(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(username, claims.get("username"));
        assertEquals(role, claims.get("role"));
        assertEquals("access", claims.get("type"));
        
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void generateRefreshToken_Success() {
        // Given
        Long userId = 1L;
        String username = "testuser";

        // When
        String token = jwtService.generateRefreshToken(userId, username);

        // Then
        assertNotNull(token);
        
        Claims claims = jwtService.parseClaims(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    void parseUserId_Success() {
        // Given
        Long userId = 1L;
        String token = jwtService.generateAccessToken(userId, "testuser", "USER");

        // When
        Long parsedUserId = jwtService.parseUserId(token);

        // Then
        assertEquals(userId, parsedUserId);
    }

    @Test
    void getTokenType_AccessToken() {
        // Given
        String token = jwtService.generateAccessToken(1L, "testuser", "USER");

        // When
        String type = jwtService.getTokenType(token);

        // Then
        assertEquals("access", type);
    }

    @Test
    void getTokenType_RefreshToken() {
        // Given
        String token = jwtService.generateRefreshToken(1L, "testuser");

        // When
        String type = jwtService.getTokenType(token);

        // Then
        assertEquals("refresh", type);
    }

    @Test
    void invalidateToken_Success() {
        // Given
        Long userId = 1L;

        // When
        jwtService.invalidateToken(userId);

        // Then
        verify(redisTemplate).delete(contains("dp:token:refresh:"));
    }

    @Test
    void getRemainingTime_ValidToken() {
        // Given
        String token = jwtService.generateAccessToken(1L, "testuser", "USER");

        // When
        long remaining = jwtService.getRemainingTime(token);

        // Then
        assertTrue(remaining > 0);
        assertTrue(remaining <= 120);
    }
}
