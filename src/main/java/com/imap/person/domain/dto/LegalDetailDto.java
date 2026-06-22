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

package com.imap.person.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LegalDetailDto(
    UUID personId,
    UUID entityTypeId,
    LocalDate constitutionDate,
    UUID registrationBodyId,
    String registrationNumber,
    LocalDate registrationDate,
    UUID capitalCurrencyId,
    BigDecimal capitalSuscripto,
    BigDecimal capitalIntegrado,
    Long sharesCount,
    BigDecimal shareNominalValue,
    Integer exerciseEndMonth,
    Integer exerciseEndDay,
    String objetoSocial,
    UUID auditFirmId,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
