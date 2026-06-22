// ─── GOLDEN-RULES:BEGIN (auto · golden-rules.json · no editar a mano) ───
// REGLAS DE ORO IMAP — cumplir SIEMPRE (ver IMAP_GUIA_DESARROLLO.md):
//  • HTTP-only entre servicios (+ s2s auth; no SQL cross-service; futuro Kafka)
//  • Names en inglés
//  • UUIDv7 en ids
//  • i18n: idioma del string, no de la fila; datos (UUID, field, idioma)
//  • VtR: único canal con el frontend (front solo ve virtual)
//  • Hexagonal estricto (domain no depende de infra)
//  • No secrets en código (.env en C:\Applications, nunca hardcodear)
//  • Idempotencia en operaciones de negocio (idempotency key)
//  • [controller] DTOs, nunca exponer entidades del domain en la API
//  • [person] Master data de personas (federado por otros micros)
//  • [person] TaxId: is_current GENERATED ALWAYS
//  • [person] Lookup por CUIT; FiscalProfile ARCA/IIBB
// ─── GOLDEN-RULES:END ───

package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.FiscalProfileService;
import com.imap.person.application.service.TaxIdService;
import com.imap.person.domain.dto.CreateFiscalProfileRequest;
import com.imap.person.domain.dto.CreatePersonRequest;
import com.imap.person.domain.dto.CreateTaxIdRequest;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.domain.dto.PersonSummaryDto;
import java.util.List;
import com.imap.person.domain.model.PersonType;
import com.imap.person.domain.port.in.PersonUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
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

    /**
     * GET list (Fase 3 — tablas/reportes federados): lista registros como payloads PLANOS
     * (claves = field codes + id). Soportado para per_person; tax_id/fiscal = misma receta.
     */
    @GetMapping("/{entityCode}")
    public ResponseEntity<Object> listForm(@PathVariable String entityCode) {
        if ("per_person".equals(entityCode)) {
            List<Map<String, Object>> out = personUseCase.findAll().stream().map(this::summaryToForm).toList();
            return ResponseEntity.ok(out);
        }
        return ResponseEntity.status(501).body(Map.of("error", "list-form no soportado aún para: " + entityCode));
    }

    private Map<String, Object> summaryToForm(PersonSummaryDto s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.id());
        m.put("per_person_person_type", s.personType() != null ? s.personType().name() : null);
        m.put("per_person_legal_name", s.legalName());
        m.put("per_person_trade_name", s.tradeName());
        return m;
    }

    /**
     * GET form: precarga de un registro existente para EDICIÓN (payload PLANO, claves =
     * field codes del registry). Soportado hoy para per_person; tax_id/fiscal = misma receta.
     */
    @GetMapping("/{entityCode}/{id}")
    public ResponseEntity<Object> getForm(@PathVariable String entityCode, @PathVariable UUID id) {
        if ("per_person".equals(entityCode)) {
            return personUseCase.findById(id)
                .map(p -> ResponseEntity.ok((Object) personToForm(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(501).body(Map.of("error", "get-form no soportado aún para: " + entityCode));
    }

    /** PATCH form: actualiza un registro existente vía el dominio del micro. */
    @PatchMapping("/{entityCode}/{id}")
    public ResponseEntity<Object> updateForm(@PathVariable String entityCode, @PathVariable UUID id,
                                             @RequestBody Map<String, Object> form) {
        if ("per_person".equals(entityCode)) {
            String typeStr = str(form, "per_person_person_type");
            if (typeStr == null) return bad("per_person_person_type es obligatorio");
            PersonType type;
            try { type = PersonType.valueOf(typeStr); }
            catch (IllegalArgumentException e) { return bad("per_person_person_type inválido: " + typeStr); }
            CreatePersonRequest req = new CreatePersonRequest(
                type, str(form, "per_person_legal_name"), str(form, "per_person_trade_name"),
                uuid(form, "per_person_country_id"));
            return ResponseEntity.ok(personUseCase.update(id, req));
        }
        return ResponseEntity.status(501).body(Map.of("error", "update-form no soportado aún para: " + entityCode));
    }

    private Map<String, Object> personToForm(PersonDto p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("per_person_person_type", p.personType() != null ? p.personType().name() : null);
        m.put("per_person_legal_name", p.legalName());
        m.put("per_person_trade_name", p.tradeName());
        m.put("per_person_country_id", p.countryId() != null ? p.countryId().toString() : null);
        return m;
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
