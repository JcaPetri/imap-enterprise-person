// ─── GOLDEN-RULES:BEGIN (auto · golden-rules.json · no editar a mano) ───
// REGLAS DE ORO IMAP — cumplir SIEMPRE (ver IMAP_GUIA_DESARROLLO.md):
//  • HTTP-only entre servicios (+ s2s auth; no SQL cross-service; futuro Kafka)
//  • Names en inglés
//  • UUIDv7 en ids
//  • i18n: idioma del string, no de la fila; datos (UUID, field, idioma)
//  • VtR: único canal con el frontend (front solo ve virtual)
//  • Hexagonal estricto (domain no depende de infra)
//  • No secrets en código (.env en C:\Applications, nunca hardcodear)
//  • Idempotencia en operaciones de negocio (idempotency key)
//  • [person] Master data de personas (federado por otros micros)
//  • [person] TaxId: is_current GENERATED ALWAYS
//  • [person] Lookup por CUIT; FiscalProfile ARCA/IIBB
// ─── GOLDEN-RULES:END ───

package com.imap.person.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imap.platform.security.BearerTokenHolder;
import com.imap.platform.security.JwtAccessTokenValidator;
import com.imap.platform.security.UserContext;
import com.imap.platform.security.UserContextHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Extrae el JWT del header Authorization, lo valida y publica UserContext.
 * Sin token → anonymous (útil para smoke tests).
 * Token inválido → 401.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String HEADER        = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtAccessTokenValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthFilter(JwtAccessTokenValidator validator) {
        this.validator = validator;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        if (path != null && path.startsWith("/actuator")) return true;
        return "OPTIONS".equalsIgnoreCase(req.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = req.getHeader(HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        if (!validator.isEnabled()) {
            log.warn("Authorization header present but JWT validator disabled — {} {} → anonymous",
                     req.getMethod(), req.getRequestURI());
            chain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Claims claims;
        try {
            claims = validator.validate(token);
        } catch (JwtException e) {
            log.warn("JWT validation failed for {} {}: {}", req.getMethod(), req.getRequestURI(), e.getMessage());
            sendUnauthorized(res, "invalid_token", e.getMessage());
            return;
        }

        UserContext ctx;
        try {
            ctx = buildContext(claims);
        } catch (IllegalArgumentException e) {
            log.warn("JWT structure invalid for {} {}: {}", req.getMethod(), req.getRequestURI(), e.getMessage());
            sendUnauthorized(res, "invalid_token_structure", e.getMessage());
            return;
        }

        // Populate Spring Security context so anyRequest().authenticated() passes
        var springAuth = new UsernamePasswordAuthenticationToken(
                ctx.userId().toString(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(springAuth);

        UserContextHolder.set(ctx);
        BearerTokenHolder.set(token);
        log.debug("Authenticated user={} ({})", ctx.userId(), ctx.email());
        try {
            chain.doFilter(req, res);
        } finally {
            SecurityContextHolder.clearContext();
            UserContextHolder.clear();
            BearerTokenHolder.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private UserContext buildContext(Claims claims) {
        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) throw new IllegalArgumentException("missing 'sub' claim");
        UUID userId;
        try {
            userId = UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("'sub' is not a UUID: " + sub);
        }
        String email = claims.get("email", String.class);
        boolean impersonation = Boolean.TRUE.equals(claims.get("impersonation", Boolean.class));

        Map<String, List<String>> permsByTenant = Collections.emptyMap();
        Object raw = claims.get("permissionsByTenant");
        if (raw instanceof Map<?, ?> rawMap) {
            permsByTenant = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : rawMap.entrySet()) {
                if (!(e.getKey() instanceof String tenantId)) continue;
                List<String> perms = new ArrayList<>();
                if (e.getValue() instanceof List<?> rawList) {
                    for (Object item : rawList) {
                        if (item instanceof String s) perms.add(s);
                    }
                }
                permsByTenant.put(tenantId, perms);
            }
        }
        return new UserContext(userId, email, permsByTenant, impersonation);
    }

    private void sendUnauthorized(HttpServletResponse res, String error, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of("error", error, "message", message));
    }
}
