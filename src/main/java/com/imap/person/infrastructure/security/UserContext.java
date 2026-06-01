package com.imap.person.infrastructure.security;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contexto de usuario autenticado — extraído del JWT y disponible durante
 * el procesamiento de la request via UserContextHolder (ThreadLocal).
 */
public record UserContext(
        UUID userId,
        String email,
        Map<String, List<String>> permissionsByTenant,
        boolean impersonation
) {}
