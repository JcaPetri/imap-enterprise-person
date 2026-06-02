package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.AddressService;
import com.imap.person.domain.dto.AddressDto;
import com.imap.person.domain.dto.CreateAddressRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /** POST /v1/persons/{personId}/addresses */
    @PostMapping("/v1/persons/{personId}/addresses")
    public ResponseEntity<AddressDto> create(
            @PathVariable UUID personId,
            @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(addressService.create(personId, req));
    }

    /** GET /v1/persons/{personId}/addresses */
    @GetMapping("/v1/persons/{personId}/addresses")
    public ResponseEntity<List<AddressDto>> listByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(addressService.listByPerson(personId));
    }
}
