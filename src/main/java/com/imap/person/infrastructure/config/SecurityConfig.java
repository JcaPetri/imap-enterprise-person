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

package com.imap.person.infrastructure.config;

import com.imap.person.infrastructure.security.JwtAuthFilter;
import com.imap.platform.tenant.TenantContextFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Spring Security configuration for imap-person.
 *
 * Public endpoints: /ping, /actuator/**
 * All other endpoints require a valid Bearer JWT.
 * Stateless — no server-side sessions.
 *
 * Filter ordering strategy:
 *  JwtAuthFilter and TenantContextFilter are @Component beans but their
 *  servlet auto-registration is DISABLED via FilterRegistrationBean.enabled=false.
 *  They run ONLY inside Spring Security's filter chain (via addFilterBefore),
 *  which ensures SecurityContextHolderFilter runs first and the SecurityContext
 *  set by JwtAuthFilter is visible to the AuthorizationFilter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TenantContextFilter tenantContextFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          TenantContextFilter tenantContextFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.tenantContextFilter = tenantContextFilter;
    }

    /**
     * Prevent JwtAuthFilter from being registered as a standalone servlet filter.
     * It must run only inside Spring Security's chain so SecurityContextHolderFilter
     * initialises the context first.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    /**
     * Same: TenantContextFilter runs inside Security chain only.
     */
    @Bean
    public FilterRegistrationBean<TenantContextFilter> tenantFilterRegistration(TenantContextFilter filter) {
        FilterRegistrationBean<TenantContextFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ping").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"error\":\"Unauthorized\",\"message\":\"Valid Bearer token required.\"}");
                })
            )
            // Filtros DESPUÉS del ExceptionTranslationFilter (antes de AuthorizationFilter) →
            // sus respuestas (401/403) sobreviven. jwt primero (SecurityContext), luego tenant.
            .addFilterBefore(jwtAuthFilter, AuthorizationFilter.class)
            .addFilterBefore(tenantContextFilter, AuthorizationFilter.class);

        return http.build();
    }
}
