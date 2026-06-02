package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FiscalProfileDto(
    UUID id,
    UUID personId,
    String organismCode,
    String fiscalPosition,
    LocalDate registeredSince,
    LocalDate registeredTo,
    boolean active,
    OffsetDateTime createdAt
) {}
