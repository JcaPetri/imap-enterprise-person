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

import com.imap.person.application.service.ContactService;
import com.imap.person.domain.dto.ContactDto;
import com.imap.person.domain.dto.CreateContactRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /** POST /v1/persons/{personId}/contacts */
    @PostMapping("/v1/persons/{personId}/contacts")
    public ResponseEntity<ContactDto> create(
            @PathVariable UUID personId,
            @Valid @RequestBody CreateContactRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(contactService.create(personId, req));
    }

    /** GET /v1/persons/{personId}/contacts */
    @GetMapping("/v1/persons/{personId}/contacts")
    public ResponseEntity<List<ContactDto>> listByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(contactService.listByPerson(personId));
    }
}
