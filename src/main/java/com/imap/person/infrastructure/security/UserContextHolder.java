package com.imap.person.infrastructure.security;

/**
 * ThreadLocal holder para UserContext — permite que cualquier capa del
 * microservicio acceda al usuario autenticado sin propagación explícita.
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContext ctx) { HOLDER.set(ctx); }

    public static UserContext get() { return HOLDER.get(); }

    public static void clear() { HOLDER.remove(); }
}
