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
//  • [controller] DTOs, nunca exponer entidades del domain en la API
//  • [person] Master data de personas (federado por otros micros)
//  • [person] TaxId: is_current GENERATED ALWAYS
//  • [person] Lookup por CUIT; FiscalProfile ARCA/IIBB
// ─── GOLDEN-RULES:END ───

package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.LegalDetailService;
import com.imap.person.domain.dto.LegalDetailDto;
import com.imap.person.domain.dto.UpsertLegalDetailRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class LegalDetailController {

    private final LegalDetailService legalDetailService;

    public LegalDetailController(LegalDetailService legalDetailService) {
        this.legalDetailService = legalDetailService;
    }

    /** PUT /v1/persons/{personId}/legal-detail */
    @PutMapping("/v1/persons/{personId}/legal-detail")
    public ResponseEntity<LegalDetailDto> upsert(
            @PathVariable UUID personId,
            @Valid @RequestBody UpsertLegalDetailRequest req) {
        return ResponseEntity.ok(legalDetailService.upsert(personId, req));
    }

    /** GET /v1/persons/{personId}/legal-detail */
    @GetMapping("/v1/persons/{personId}/legal-detail")
    public ResponseEntity<LegalDetailDto> get(@PathVariable UUID personId) {
        return legalDetailService.findByPerson(personId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
