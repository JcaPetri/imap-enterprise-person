package com.imap.person.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Valida access tokens JWT (HS256) emitidos por el IAM microservice.
 * Comparte el mismo secret simétrico (JWT_ACCESS_SECRET).
 */
@Component
public class JwtAccessTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtAccessTokenValidator.class);

    @Value("${jwt.access.secret:}")
    private String accessSecret;

    private boolean enabled;

    @PostConstruct
    void init() {
        if (accessSecret == null || accessSecret.isBlank()) {
            enabled = false;
            log.warn("JwtAccessTokenValidator DISABLED — jwt.access.secret/JWT_ACCESS_SECRET not set. " +
                     "Requests with Authorization header will be ignored (anonymous mode).");
            return;
        }
        if (accessSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                "jwt.access.secret must be >=32 chars (256 bits) for HS256. Current length: "
                + accessSecret.length());
        }
        enabled = true;
        log.info("JwtAccessTokenValidator enabled — HS256 secret loaded ({} bytes).",
                 accessSecret.getBytes(StandardCharsets.UTF_8).length);
    }

    public boolean isEnabled() { return enabled; }

    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }
}
