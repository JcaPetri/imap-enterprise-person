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
