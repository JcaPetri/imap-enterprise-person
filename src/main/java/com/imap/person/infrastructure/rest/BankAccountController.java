package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.BankAccountService;
import com.imap.person.domain.dto.BankAccountDto;
import com.imap.person.domain.dto.CreateBankAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    /** POST /v1/persons/{personId}/bank-accounts */
    @PostMapping("/v1/persons/{personId}/bank-accounts")
    public ResponseEntity<BankAccountDto> create(
            @PathVariable UUID personId,
            @Valid @RequestBody CreateBankAccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bankAccountService.create(personId, req));
    }

    /** GET /v1/persons/{personId}/bank-accounts */
    @GetMapping("/v1/persons/{personId}/bank-accounts")
    public ResponseEntity<List<BankAccountDto>> listByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(bankAccountService.listByPerson(personId));
    }
}
