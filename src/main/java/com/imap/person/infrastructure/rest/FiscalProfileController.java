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

import com.imap.person.application.service.FiscalProfileService;
import com.imap.person.domain.dto.CreateFiscalProfileRequest;
import com.imap.person.domain.dto.FiscalProfileDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class FiscalProfileController {

    private final FiscalProfileService fiscalProfileService;

    public FiscalProfileController(FiscalProfileService fiscalProfileService) {
        this.fiscalProfileService = fiscalProfileService;
    }

    /**
     * POST /v1/persons/{personId}/fiscal-profiles
     * Add a fiscal profile (position at organism) to a person.
     */
    @PostMapping("/v1/persons/{personId}/fiscal-profiles")
    public ResponseEntity<FiscalProfileDto> create(
            @PathVariable UUID personId,
            @Valid @RequestBody CreateFiscalProfileRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(fiscalProfileService.create(personId, req));
    }

    /**
     * GET /v1/persons/{personId}/fiscal-profiles
     * List active fiscal profiles for a person.
     */
    @GetMapping("/v1/persons/{personId}/fiscal-profiles")
    public ResponseEntity<List<FiscalProfileDto>> listByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(fiscalProfileService.listByPerson(personId));
    }
}
