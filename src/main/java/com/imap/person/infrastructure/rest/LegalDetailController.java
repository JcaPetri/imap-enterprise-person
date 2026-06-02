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
