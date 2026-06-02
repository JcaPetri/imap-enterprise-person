package com.imap.person.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateContactRequest(
    UUID contactTypeId,
    UUID labelId,
    @NotBlank String value,
    boolean primary
) {}
