package com.imap.person.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaxIdRequest(
    @NotBlank String documentTypeKey,
    @NotBlank String taxIdValue,
    UUID countryId,
    LocalDate validFrom
) {}
