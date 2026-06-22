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

import com.imap.person.application.service.NaturalDetailService;
import com.imap.person.domain.dto.NaturalDetailDto;
import com.imap.person.domain.dto.UpsertNaturalDetailRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class NaturalDetailController {

    private final NaturalDetailService naturalDetailService;

    public NaturalDetailController(NaturalDetailService naturalDetailService) {
        this.naturalDetailService = naturalDetailService;
    }

    /** PUT /v1/persons/{personId}/natural-detail */
    @PutMapping("/v1/persons/{personId}/natural-detail")
    public ResponseEntity<NaturalDetailDto> upsert(
            @PathVariable UUID personId,
            @Valid @RequestBody UpsertNaturalDetailRequest req) {
        return ResponseEntity.ok(naturalDetailService.upsert(personId, req));
    }

    /** GET /v1/persons/{personId}/natural-detail */
    @GetMapping("/v1/persons/{personId}/natural-detail")
    public ResponseEntity<NaturalDetailDto> get(@PathVariable UUID personId) {
        return naturalDetailService.findByPerson(personId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
