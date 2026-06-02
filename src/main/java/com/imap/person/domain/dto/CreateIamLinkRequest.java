package com.imap.person.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateIamLinkRequest(
    @NotBlank String iamEntityType,
    @NotNull UUID iamEntityId,
    boolean primary
) {}
