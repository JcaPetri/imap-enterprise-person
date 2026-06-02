package com.imap.person.domain.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContactDto(
    UUID id,
    UUID personId,
    UUID contactTypeId,
    UUID labelId,
    String value,
    boolean primary,
    boolean validated,
    boolean active,
    OffsetDateTime createdAt
) {}
