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

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request para crear un domicilio en per_address_tbl.
 * latitude / longitude opcionales: si ambos presentes se graba geometry(POINT,4326).
 */
public record CreateAddressRequest(
        UUID    addressTypeId,   // FK → per_dataelement_tbl WHERE category='ADDRESS_TYPE' (nullable)
        String  street,
        String  streetNumber,
        String  floor,
        String  apartment,
        String  city,
        UUID    provinceId,      // soft FK → system.sys_province_tbl (nullable)
        UUID    countryId,       // soft FK → system.sys_country_tbl (nullable)
        String  postalCode,
        LocalDate validFrom,
        LocalDate validTo,
        Double  latitude,        // WGS-84 (nullable)
        Double  longitude        // WGS-84 (nullable)
) {}
