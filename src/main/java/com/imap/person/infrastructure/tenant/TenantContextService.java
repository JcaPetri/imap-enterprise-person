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

package com.imap.person.infrastructure.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Setea app.current_tenant_id y app.current_user_id para las políticas RLS
 * de PostgreSQL. Debe llamarse dentro de una transacción activa.
 *
 * Mismo patrón que manufacturing / inventory — SET LOCAL es suficiente
 * porque la tx abarca toda la request.
 */
@Service
public class TenantContextService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Setea el contexto RLS para tenant + user.
     * Llamar al inicio de cada operación de dominio con tenant activo.
     */
    public void setContext(UUID tenantId, UUID userId) {
        em.createNativeQuery(
            "SELECT set_config('app.current_tenant_id', :tid, TRUE), " +
            "       set_config('app.current_user_id',   :uid, TRUE)"
        )
          .setParameter("tid", tenantId.toString())
          .setParameter("uid", userId != null ? userId.toString() : "")
          .getSingleResult();
    }

    /**
     * Sobrecarga sin userId — para jobs / scheduled tasks sin contexto de usuario.
     */
    public void setContext(UUID tenantId) {
        em.createNativeQuery(
            "SELECT set_config('app.current_tenant_id', :tid, TRUE), " +
            "       set_config('app.current_user_id',   '', TRUE)"
        )
          .setParameter("tid", tenantId.toString())
          .getSingleResult();
    }
}
