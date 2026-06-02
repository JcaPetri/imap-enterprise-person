package com.imap.person.domain.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IamLinkDto(
    UUID id,
    UUID personId,
    String iamEntityType,
    UUID iamEntityId,
    boolean primary,
    OffsetDateTime linkedAt,
    boolean active,
    OffsetDateTime createdAt
) {}
