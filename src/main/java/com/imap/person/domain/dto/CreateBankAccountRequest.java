package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBankAccountRequest(
    UUID bankCodeId,
    UUID accountTypeId,
    UUID currencyId,
    String cbu,
    String cvu,
    String alias,
    boolean primary,
    LocalDate validFrom,
    LocalDate validTo
) {}
