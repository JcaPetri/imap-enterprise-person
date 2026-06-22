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

import com.imap.person.application.service.IamLinkService;
import com.imap.person.domain.dto.CreateIamLinkRequest;
import com.imap.person.domain.dto.IamLinkDto;
import com.imap.person.domain.dto.PersonDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class IamLinkController {

    private final IamLinkService iamLinkService;

    public IamLinkController(IamLinkService iamLinkService) {
        this.iamLinkService = iamLinkService;
    }

    /** POST /v1/persons/{personId}/iam-links */
    @PostMapping("/v1/persons/{personId}/iam-links")
    public ResponseEntity<IamLinkDto> create(
            @PathVariable UUID personId,
            @Valid @RequestBody CreateIamLinkRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(iamLinkService.create(personId, req));
    }

    /** GET /v1/persons/{personId}/iam-links */
    @GetMapping("/v1/persons/{personId}/iam-links")
    public ResponseEntity<List<IamLinkDto>> listByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(iamLinkService.listByPerson(personId));
    }

    /**
     * GET /v1/persons/by-iam/{iamEntityType}/{iamEntityId}
     * Reverse lookup: given an IAM entity, return the linked Person.
     */
    @GetMapping("/v1/persons/by-iam/{iamEntityType}/{iamEntityId}")
    public ResponseEntity<PersonDto> findByIamEntity(
            @PathVariable String iamEntityType,
            @PathVariable UUID iamEntityId) {
        return ResponseEntity.ok(
            iamLinkService.findPersonByIamEntity(iamEntityType, iamEntityId));
    }
}
