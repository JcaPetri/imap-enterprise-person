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

import com.imap.person.infrastructure.entity.PerTaxIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerTaxIdJpaRepository extends JpaRepository<PerTaxIdEntity, UUID> {

    List<PerTaxIdEntity> findByPersonIdAndActiveTrue(UUID personId);

    @Query("SELECT t FROM PerTaxIdEntity t WHERE t.taxIdValue = :value AND t.current = true")
    Optional<PerTaxIdEntity> findCurrentByValue(@Param("value") String value);

    boolean existsByTaxIdValueAndCurrentTrue(String taxIdValue);

    /** Look up an active tax ID record joining through per_document_type_tbl by dataelement_key. */
    @Query(value = "SELECT t.* FROM person.per_tax_id_tbl t " +
                   "JOIN person.per_document_type_tbl d ON t.document_type_id = d.id " +
                   "WHERE d.dataelement_key = :docTypeKey " +
                   "AND t.tax_id_value = :value AND t.is_current = true",
           nativeQuery = true)
    Optional<PerTaxIdEntity> findCurrentByDocTypeKeyAndValue(
            @Param("docTypeKey") String docTypeKey,
            @Param("value") String value);

    /** Check whether an active record exists for a specific document_type_id + value. */
    boolean existsByDocumentTypeIdAndTaxIdValueAndCurrentTrue(UUID documentTypeId, String taxIdValue);
}
