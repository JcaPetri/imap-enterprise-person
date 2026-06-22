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

import com.imap.person.application.service.RelationshipService;
import com.imap.person.domain.dto.CreateRelationshipRequest;
import com.imap.person.domain.dto.RelationshipDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    /** POST /v1/persons/{fromPersonId}/relationships */
    @PostMapping("/v1/persons/{fromPersonId}/relationships")
    public ResponseEntity<RelationshipDto> create(
            @PathVariable UUID fromPersonId,
            @Valid @RequestBody CreateRelationshipRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(relationshipService.create(fromPersonId, req));
    }

    /** GET /v1/persons/{fromPersonId}/relationships */
    @GetMapping("/v1/persons/{fromPersonId}/relationships")
    public ResponseEntity<List<RelationshipDto>> listByPerson(
            @PathVariable UUID fromPersonId) {
        return ResponseEntity.ok(relationshipService.listByPerson(fromPersonId));
    }
}
