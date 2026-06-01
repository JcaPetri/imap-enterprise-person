package com.imap.person.domain.dto;

import com.imap.person.domain.model.PersonType;
import java.util.UUID;

public record PersonSummaryDto(
    UUID id,
    PersonType personType,
    String legalName,
    String tradeName,
    String primaryTaxId
) {}
