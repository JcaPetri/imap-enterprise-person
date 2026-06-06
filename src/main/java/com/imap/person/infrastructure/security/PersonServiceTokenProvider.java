package com.imap.person.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Emite un JWT firmado con el shared secret (HS256, mismo de IAM) para autenticar
 * llamadas s2s desde person a system (Schema Registry) en threads sin request HTTP.
 * Cachea el token; regenera cuando faltan &lt;5min para expirar.
 */
@Component
public class PersonServiceTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(PersonServiceTokenProvider.class);
    private static final Duration TOKEN_TTL = Duration.ofHours(1);
    private static final Duration RENEW_BEFORE = Duration.ofMinutes(5);
    private static final UUID SYSTEM_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PERSON_SERVICE_USER_ID =
        UUID.nameUUIDFromBytes("person-service-account".getBytes(StandardCharsets.UTF_8));

    @Value("${jwt.access.secret:}")
    private String accessSecret;

    private SecretKey key;
    private volatile String cachedToken;
    private volatile Instant cachedExpiry;

    @PostConstruct
    void init() {
        if (accessSecret == null || accessSecret.isBlank()) {
            log.warn("PersonServiceTokenProvider DISABLED — jwt.access.secret no seteado. s2s fallará 401.");
            return;
        }
        this.key = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        log.info("PersonServiceTokenProvider enabled — service user id={}", PERSON_SERVICE_USER_ID);
    }

    /** Token de servicio con system.admin en el SYSTEM tenant. null si no hay secret. */
    public String currentToken() {
        if (key == null) return null;
        Instant now = Instant.now();
        if (cachedToken != null && cachedExpiry != null && cachedExpiry.isAfter(now.plus(RENEW_BEFORE))) {
            return cachedToken;
        }
        return regenerate(now);
    }

    private synchronized String regenerate(Instant now) {
        if (cachedToken != null && cachedExpiry != null && cachedExpiry.isAfter(now.plus(RENEW_BEFORE))) {
            return cachedToken;
        }
        Instant expiry = now.plus(TOKEN_TTL);
        cachedToken = Jwts.builder()
            .subject(PERSON_SERVICE_USER_ID.toString())
            .claim("email", "person@imap.internal")
            .claim("impersonation", false)
            .claim("permissionsByTenant", Map.of(SYSTEM_TENANT_ID.toString(), List.of("system.admin")))
            .issuedAt(java.util.Date.from(now))
            .expiration(java.util.Date.from(expiry))
            .signWith(key)
            .compact();
        cachedExpiry = expiry;
        return cachedToken;
    }
}
