package com.imap.person.infrastructure.tenant;

import com.imap.person.infrastructure.security.UserContext;
import com.imap.person.infrastructure.security.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Extrae el X-Tenant-Id header y lo publica en TenantContextHolder.
 * Corre después de JwtAuthFilter (order +20).
 *
 * No valida membresía aquí — eso lo hace el dominio / RLS de PostgreSQL.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-Id";

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
        String tenantHeader = req.getHeader(TENANT_HEADER);
        UUID tenantId = null;

        if (tenantHeader != null && !tenantHeader.isBlank()) {
            try {
                tenantId = UUID.fromString(tenantHeader.trim());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid X-Tenant-Id header value: {}", tenantHeader);
            }
        }

        // Fallback: si el JWT tiene un único tenant en permissionsByTenant, usarlo
        if (tenantId == null) {
            UserContext ctx = UserContextHolder.get();
            if (ctx != null && ctx.permissionsByTenant() != null
                    && ctx.permissionsByTenant().size() == 1) {
                String firstKey = ctx.permissionsByTenant().keySet().iterator().next();
                try {
                    tenantId = UUID.fromString(firstKey);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        TenantContextHolder.set(tenantId);
        try {
            chain.doFilter(req, res);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
