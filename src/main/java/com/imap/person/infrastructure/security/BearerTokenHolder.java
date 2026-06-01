package com.imap.person.infrastructure.security;

/**
 * ThreadLocal holder para el Bearer token original — usado para propagar
 * el JWT en llamadas s2s a otros microservicios (BPM, System).
 */
public final class BearerTokenHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private BearerTokenHolder() {}

    public static void set(String token) { HOLDER.set(token); }

    public static String get() { return HOLDER.get(); }

    public static void clear() { HOLDER.remove(); }
}
