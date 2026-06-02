package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AddressDto(
        UUID        id,
        UUID        personId,
        UUID        addressTypeId,
        String      street,
        String      streetNumber,
        String      floor,
        String      apartment,
        String      city,
        UUID        provinceId,
        UUID        countryId,
        String      postalCode,
        LocalDate   validFrom,
        LocalDate   validTo,
        Double      latitude,     // null si location es null
        Double      longitude,    // null si location es null
        boolean     active,
        OffsetDateTime createdAt
) {}
