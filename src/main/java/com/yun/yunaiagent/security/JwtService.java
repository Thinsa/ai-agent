package com.yun.yunaiagent.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey secretKey;

    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration:86400000}") long expirationMillis
    ) {
        String resolvedSecret = secret == null || secret.length() < 32
                ? "local-development-secret-key-change-me-please"
                : secret;
        this.secretKey = Keys.hmacShaKeyFor(resolvedSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username) {
        return createToken(username).token();
    }

    public TokenInfo createToken(String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMillis);
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new TokenInfo(token, expiresAt.toEpochMilli(), expirationMillis);
    }

    public String parseUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public Optional<TokenValidation> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new TokenValidation(
                    claims.getSubject(),
                    claims.getExpiration().toInstant().toEpochMilli()
            ));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public record TokenInfo(String token, long expiresAt, long expiresIn) {
    }

    public record TokenValidation(String username, long expiresAt) {
    }
}
