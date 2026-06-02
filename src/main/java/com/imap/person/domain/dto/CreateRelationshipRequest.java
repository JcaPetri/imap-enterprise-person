package com.imap.person.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRelationshipRequest(
    @NotNull UUID toPersonId,
    @NotNull UUID relTypeId,
    String notes,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
