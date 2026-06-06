package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.FiscalProfileService;
import com.imap.person.application.service.TaxIdService;
import com.imap.person.domain.dto.CreateFiscalProfileRequest;
import com.imap.person.domain.dto.CreatePersonRequest;
import com.imap.person.domain.dto.CreateTaxIdRequest;
import com.imap.person.domain.model.PersonType;
import com.imap.person.domain.port.in.PersonUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Schema Registry — intake de formularios (persistencia federada).
 *
 * uxdesign genera el form desde la metadata que person publicó en system; al
 * enviarlo, el action 'micro_form' postea acá el payload PLANO (claves = field codes
 * del registry). person mapea a su dominio y persiste vía sus UseCases/Services
 * (validación/audit/RLS propios). system NUNCA escribe en person.
 *
 * Entidades soportadas: per_person, per_tax_id, per_fiscal_profile.
 */
@RestController
@RequestMapping("/v1/forms")
public class FormIntakeController {

    private final PersonUseCase personUseCase;
    private final TaxIdService taxIdService;
    private final FiscalProfileService fiscalProfileService;

    public FormIntakeController(PersonUseCase personUseCase,
                                TaxIdService taxIdService,
                                FiscalProfileService fiscalProfileService) {
        this.personUseCase = personUseCase;
        this.taxIdService = taxIdService;
        this.fiscalProfileService = fiscalProfileService;
    }

    @PostMapping("/{entityCode}")
    public ResponseEntity<Object> intake(@PathVariable String entityCode,
                                         @RequestBody Map<String, Object> form) {
        switch (entityCode) {
            case "per_person":          return createPerson(form);
            case "per_tax_id":          return createTaxId(form);
            case "per_fiscal_profile":  return createFiscalProfile(form);
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "entityCode no soportado en intake: " + entityCode));
        }
    }

    private ResponseEntity<Object> createPerson(Map<String, Object> form) {
        String typeStr = str(form, "per_person_person_type");
        if (typeStr == null) return bad("per_person_person_type es obligatorio");
        PersonType type;
        try { type = PersonType.valueOf(typeStr); }
        catch (IllegalArgumentException e) { return bad("per_person_person_type inválido: " + typeStr); }
        CreatePersonRequest req = new CreatePersonRequest(
            type,
            str(form, "per_person_legal_name"),
            str(form, "per_person_trade_name"),
            uuid(form, "per_person_country_id"));
        return ResponseEntity.status(HttpStatus.CREATED).body(personUseCase.create(req));
    }

    private ResponseEntity<Object> createTaxId(Map<String, Object> form) {
        UUID personId = uuid(form, "per_tax_id_person_id");
        if (personId == null) return bad("per_tax_id_person_id es obligatorio (UUID de persona)");
        CreateTaxIdRequest req = new CreateTaxIdRequest(
            str(form, "per_tax_id_document_type_key"),
            str(form, "per_tax_id_value"),
            uuid(form, "per_tax_id_country_id"),
            date(form, "per_tax_id_valid_from"));
        return ResponseEntity.status(HttpStatus.CREATED).body(taxIdService.create(personId, req));
    }

    private ResponseEntity<Object> createFiscalProfile(Map<String, Object> form) {
        UUID personId = uuid(form, "per_fiscal_profile_person_id");
        if (personId == null) return bad("per_fiscal_profile_person_id es obligatorio (UUID de persona)");
        CreateFiscalProfileRequest req = new CreateFiscalProfileRequest(
            str(form, "per_fiscal_profile_organism_code"),
            str(form, "per_fiscal_profile_fiscal_position"),
            date(form, "per_fiscal_profile_registered_since"),
            date(form, "per_fiscal_profile_registered_to"));
        return ResponseEntity.status(HttpStatus.CREATED).body(fiscalProfileService.create(personId, req));
    }

    // ── helpers ──
    private static ResponseEntity<Object> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }
    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
    private static UUID uuid(Map<String, Object> m, String k) {
        String s = str(m, k);
        if (s == null) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
    }
    private static LocalDate date(Map<String, Object> m, String k) {
        String s = str(m, k);
        if (s == null) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
