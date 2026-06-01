package com.imap.person.domain.dto;

import com.imap.person.domain.model.PersonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePersonRequest(
    @NotNull PersonType personType,
    @NotBlank String legalName,
    String tradeName,
    UUID countryId
) {}
