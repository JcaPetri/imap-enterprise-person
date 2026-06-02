package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RelationshipDto(
    UUID id,
    UUID fromPersonId,
    UUID toPersonId,
    UUID relTypeId,
    String notes,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    boolean active,
    OffsetDateTime createdAt
) {}
