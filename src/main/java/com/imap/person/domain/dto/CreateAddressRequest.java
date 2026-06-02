package com.imap.person.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request para crear un domicilio en per_address_tbl.
 * latitude / longitude opcionales: si ambos presentes se graba geometry(POINT,4326).
 */
public record CreateAddressRequest(
        UUID    addressTypeId,   // FK → per_dataelement_tbl WHERE category='ADDRESS_TYPE' (nullable)
        String  street,
        String  streetNumber,
        String  floor,
        String  apartment,
        String  city,
        UUID    provinceId,      // soft FK → system.sys_province_tbl (nullable)
        UUID    countryId,       // soft FK → system.sys_country_tbl (nullable)
        String  postalCode,
        LocalDate validFrom,
        LocalDate validTo,
        Double  latitude,        // WGS-84 (nullable)
        Double  longitude        // WGS-84 (nullable)
) {}
