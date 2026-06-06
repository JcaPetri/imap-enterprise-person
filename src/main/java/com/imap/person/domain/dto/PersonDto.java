package com.imap.person.domain.dto;

import com.imap.person.domain.model.PersonType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PersonDto(
    UUID id,
    UUID tenantId,
    PersonType personType,
    String legalName,
    String tradeName,
    boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    UUID countryId
) {}
