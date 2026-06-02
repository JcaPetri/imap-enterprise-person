package com.imap.person.domain.dto;

import java.time.LocalDate;

public record UpsertNaturalDetailRequest(
    LocalDate birthDate,
    String birthPlace,
    String gender,
    String maritalStatus,
    String profession,
    String educationLevel,
    String nationalityCode
) {}
