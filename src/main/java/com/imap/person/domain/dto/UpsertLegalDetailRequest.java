package com.imap.person.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpsertLegalDetailRequest(
    UUID entityTypeId,
    LocalDate constitutionDate,
    UUID registrationBodyId,
    String registrationNumber,
    LocalDate registrationDate,
    UUID capitalCurrencyId,
    BigDecimal capitalSuscripto,
    BigDecimal capitalIntegrado,
    Long sharesCount,
    BigDecimal shareNominalValue,
    Integer exerciseEndMonth,
    Integer exerciseEndDay,
    String objetoSocial,
    UUID auditFirmId
) {}
