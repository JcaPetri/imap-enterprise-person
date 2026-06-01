package com.imap.person.infrastructure.tenant;

import java.util.UUID;

/**
 * ThreadLocal holder para el tenantId activo — permite que cualquier capa
 * acceda al tenant sin propagación explícita de parámetros.
 */
public final class TenantContextHolder {

    private static final ThreadLocal<UUID> HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void set(UUID tenantId) { HOLDER.set(tenantId); }

    public static UUID get() { return HOLDER.get(); }

    public static void clear() { HOLDER.remove(); }
}
