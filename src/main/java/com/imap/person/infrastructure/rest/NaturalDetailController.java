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
