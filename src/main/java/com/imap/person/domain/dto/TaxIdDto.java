package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaxIdDto(
    UUID id,
    UUID personId,
    UUID documentTypeId,
    String documentTypeKey,
    String taxIdValue,
    LocalDate validFrom,
    LocalDate validTo,
    boolean current,
    boolean active,
    OffsetDateTime createdAt
) {}
