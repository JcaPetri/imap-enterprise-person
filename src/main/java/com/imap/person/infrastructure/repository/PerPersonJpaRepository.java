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

package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerPersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface PerPersonJpaRepository extends JpaRepository<PerPersonEntity, UUID> {

    List<PerPersonEntity> findByTenantIdAndActiveTrue(UUID tenantId);

    @Query("""
        SELECT p FROM PerPersonEntity p
        WHERE p.tenantId = :tenantId
          AND p.active = true
          AND (
            FUNCTION('similarity', p.searchKey, :query) > 0.3
            OR LOWER(p.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY FUNCTION('similarity', p.searchKey, :query) DESC
        """)
    List<PerPersonEntity> searchByName(@Param("query") String query, @Param("tenantId") UUID tenantId);
}
