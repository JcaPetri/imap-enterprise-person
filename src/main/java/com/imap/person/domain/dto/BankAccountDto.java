package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BankAccountDto(
    UUID id,
    UUID personId,
    UUID bankCodeId,
    UUID accountTypeId,
    UUID currencyId,
    String cbu,
    String cvu,
    String alias,
    boolean primary,
    boolean active,
    LocalDate validFrom,
    LocalDate validTo,
    OffsetDateTime createdAt
) {}
