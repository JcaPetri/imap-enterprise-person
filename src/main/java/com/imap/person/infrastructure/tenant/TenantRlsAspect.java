package com.imap.person.infrastructure.tenant;

import com.imap.platform.security.UserContext;
import com.imap.platform.security.UserContextHolder;
import com.imap.platform.tenant.TenantContextHolder;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * S7.1 (auditoría 2026-06-12): aplica el contexto RLS (SET LOCAL
 * app.current_tenant_id / app.current_user_id) al inicio de cada método de los
 * servicios de aplicación, DENTRO de la transacción (ver {@link
 * com.imap.person.infrastructure.config.PersistenceConfig}, que pone el advisor de
 * @Transactional en HIGHEST_PRECEDENCE). Reemplaza el cableado manual por-método.
 *
 * <p>Es INERTE mientras el rol de DB siga bypassando RLS (hoy); se vuelve efectivo
 * cuando se aplique {@code FORCE ROW LEVEL SECURITY} (paso 4 del rollout S7).
 * Guardado con try/catch para no afectar el request si el set_config fallara.
 *
 * <p>El pointcut NO matchea {@code infrastructure.tenant} → sin recursión al llamar
 * a {@link TenantContextService}.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantRlsAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantRlsAspect.class);

    private final TenantContextService tenantContextService;

    public TenantRlsAspect(TenantContextService tenantContextService) {
        this.tenantContextService = tenantContextService;
    }

    @Before("execution(* com.imap.person.application.service..*(..))")
    public void applyTenantContext() {
        UUID tenant = TenantContextHolder.get();
        if (tenant == null) return;
        UserContext user = UserContextHolder.get();
        try {
            tenantContextService.setContext(tenant, user != null ? user.userId() : null);
        } catch (Exception e) {
            log.debug("RLS setContext skipped: {}", e.getMessage());
        }
    }
}
