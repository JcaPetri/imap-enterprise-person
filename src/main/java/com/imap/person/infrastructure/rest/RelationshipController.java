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
