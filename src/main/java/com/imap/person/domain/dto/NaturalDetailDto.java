package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NaturalDetailDto(
    UUID personId,
    LocalDate birthDate,
    String birthPlace,
    String gender,
    String maritalStatus,
    String profession,
    String educationLevel,
    String nationalityCode,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
