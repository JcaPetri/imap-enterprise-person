package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.TaxIdService;
import com.imap.person.domain.dto.CreateTaxIdRequest;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.domain.dto.TaxIdDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class TaxIdController {

    private final TaxIdService taxIdService;

    public TaxIdController(TaxIdService taxIdService) {
        this.taxIdService = taxIdService;
    }

    /**
     * POST /v1/persons/{personId}/tax-ids
     * Add a tax identification to an existing person.
     */
    @PostMapping("/v1/persons/{personId}/tax-ids")
    public ResponseEntity<TaxIdDto> create(
            @PathVariable UUID personId,
            @Valid @RequestBody CreateTaxIdRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxIdService.create(personId, req));
    }

    /**
     * GET /v1/persons/{personId}/tax-ids
     * List active tax IDs for a person.
     */
    @GetMapping("/v1/persons/{personId}/tax-ids")
    public ResponseEntity<List<TaxIdDto>> listByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(taxIdService.listByPerson(personId));
    }

    /**
     * GET /v1/persons/by-cuit/{cuit}
     * Find a person by their active CUIT number.
     */
    @GetMapping("/v1/persons/by-cuit/{cuit}")
    public ResponseEntity<PersonDto> findByCuit(@PathVariable String cuit) {
        return ResponseEntity.ok(taxIdService.findByCuit(cuit));
    }
}
