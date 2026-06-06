package com.imap.person.infrastructure.rest;

import com.imap.person.domain.dto.CreatePersonRequest;
import com.imap.person.domain.model.PersonType;
import com.imap.person.domain.port.in.PersonUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Schema Registry — intake de formularios (persistencia federada).
 *
 * uxdesign genera el form desde la metadata que person publicó en system; al
 * enviarlo, el action 'micro_form' postea acá el payload PLANO (claves = field codes
 * del registry, ej "per_person_legal_name"). person mapea a su dominio y persiste
 * vía su UseCase (validación/audit/RLS propios). system NUNCA escribe en person.
 *
 * Extensible por entityCode (hoy per_person; tax_id/fiscal_profile = siguiente).
 */
@RestController
@RequestMapping("/v1/forms")
public class FormIntakeController {

    private final PersonUseCase personUseCase;

    public FormIntakeController(PersonUseCase personUseCase) {
        this.personUseCase = personUseCase;
    }

    @PostMapping("/{entityCode}")
    public ResponseEntity<Object> intake(@PathVariable String entityCode,
                                         @RequestBody Map<String, Object> form) {
        if ("per_person".equals(entityCode)) {
            String typeStr = str(form, "per_person_person_type");
            if (typeStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "per_person_person_type es obligatorio"));
            }
            PersonType type;
            try {
                type = PersonType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "per_person_person_type inválido: " + typeStr));
            }
            CreatePersonRequest req = new CreatePersonRequest(
                type,
                str(form, "per_person_legal_name"),
                str(form, "per_person_trade_name"),
                uuid(form, "per_person_country_id"));
            return ResponseEntity.status(HttpStatus.CREATED).body(personUseCase.create(req));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "entityCode no soportado en intake: " + entityCode));
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
}
