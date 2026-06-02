package com.imap.person.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateFiscalProfileRequest(
    @NotBlank String organismCode,
    @NotBlank String fiscalPosition,
    LocalDate registeredSince,
    LocalDate registeredTo
) {}
