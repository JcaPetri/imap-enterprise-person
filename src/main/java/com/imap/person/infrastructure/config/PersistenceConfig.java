package com.imap.person.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * S7.1 (auditoría 2026-06-12): fuerza el advisor de @Transactional a
 * HIGHEST_PRECEDENCE para que el {@code TenantRlsAspect} (LOWEST_PRECEDENCE) corra
 * DENTRO de la transacción → el {@code SET LOCAL app.current_tenant_id} queda en la
 * misma transacción que las queries de los repositorios.
 */
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class PersistenceConfig {
}
