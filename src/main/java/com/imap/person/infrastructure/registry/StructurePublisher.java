package com.imap.person.infrastructure.registry;

import com.imap.person.infrastructure.security.PersonServiceTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema Registry — productor (person). Al arrancar, person AUTO-PUBLICA la estructura
 * (metadata) de sus entidades a system (fuente única de verdad). Los DATOS y la
 * persistencia quedan en person (federado). Idempotente (upsert por code en cada boot),
 * best-effort (si system no responde, loguea y sigue — no rompe el arranque).
 *
 * Codes prefijados `per_*` para no colisionar con el diccionario global de field_def.
 * "required" es flag estructural; los validatorPresets son códigos del catálogo de system.
 */
@Component
public class StructurePublisher {

    private static final Logger log = LoggerFactory.getLogger(StructurePublisher.class);
    private static final String SYSTEM_TENANT = "00000000-0000-0000-0000-000000000001";

    private final PersonServiceTokenProvider tokenProvider;

    @Value("${imap.system.base-url:http://localhost:8092/imap/system}")
    private String systemBaseUrl;

    @Value("${imap.registry.publish.enabled:true}")
    private boolean enabled;

    public StructurePublisher(PersonServiceTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishOnStartup() {
        if (!enabled) { log.info("StructurePublisher deshabilitado (imap.registry.publish.enabled=false)"); return; }
        String token = tokenProvider.currentToken();
        if (token == null) { log.warn("StructurePublisher: sin token de servicio (jwt.access.secret) — no publica"); return; }

        WebClient client = WebClient.builder().baseUrl(systemBaseUrl).build();
        for (Map<String, Object> descriptor : descriptors()) {
            String code = ((Map<?, ?>) descriptor.get("entity")).get("code").toString();
            try {
                String resp = client.post()
                    .uri("/v1/registry/structures")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-Tenant-Id", SYSTEM_TENANT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(descriptor)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));
                log.info("Schema Registry: publicada estructura '{}' → {}", code, resp);
            } catch (Exception e) {
                log.warn("Schema Registry: falló publicar '{}': {}", code, e.getMessage());
            }
        }
    }

    // ── descriptores (hand-declared; futuro: auto-derive de las entities JPA) ──

    private List<Map<String, Object>> descriptors() {
        List<Map<String, Object>> out = new ArrayList<>();

        out.add(entity("per_person", "Persona", "person", List.of(
            field("per_person_person_type", "Tipo de persona", "string", 1, true, null, List.of("ValidStringMax30")),
            field("per_person_legal_name",  "Razón social / Nombre", "string", 2, true, null, List.of()),
            field("per_person_trade_name",  "Nombre de fantasía", "string", 3, false, null, List.of()),
            field("per_person_country_id",  "País", "fk", 4, false, "country", List.of())
        )));

        out.add(entity("per_tax_id", "Identificación fiscal", "person", List.of(
            field("per_tax_id_person_id",          "Persona", "fk", 1, true, "per_person", List.of()),
            field("per_tax_id_document_type_key",  "Tipo de documento (CUIT/DNI/...)", "string", 2, true, null, List.of("ValidStringMax30")),
            field("per_tax_id_value",              "Número fiscal", "string", 3, true, null, List.of()),
            field("per_tax_id_country_id",         "País", "fk", 4, false, "country", List.of()),
            field("per_tax_id_valid_from",         "Vigente desde", "date", 5, false, null, List.of())
        )));

        out.add(entity("per_fiscal_profile", "Perfil fiscal", "person", List.of(
            field("per_fiscal_profile_person_id",        "Persona", "fk", 1, true, "per_person", List.of()),
            field("per_fiscal_profile_organism_code",    "Organismo", "string", 2, true, null, List.of("ValidStringMax30")),
            field("per_fiscal_profile_fiscal_position",  "Condición fiscal", "string", 3, true, null, List.of("ValidStringMax30")),
            field("per_fiscal_profile_registered_since", "Inscripto desde", "date", 4, false, null, List.of())
        )));

        return out;
    }

    private Map<String, Object> entity(String code, String name, String micro, List<Map<String, Object>> fields) {
        Map<String, Object> ent = new LinkedHashMap<>();
        ent.put("code", code);
        ent.put("name", name);
        ent.put("description", "Publicado por " + micro + " (Schema Registry)");
        ent.put("tableType", "convencional");
        ent.put("schemaName", "person");
        ent.put("microserviceCode", micro);
        ent.put("upsertPolicy", "error");
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("entity", ent);
        req.put("fields", fields);
        return req;
    }

    private Map<String, Object> field(String code, String name, String type, int order,
                                      boolean required, String fkEntityCode, List<String> presets) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("code", code);
        f.put("name", name);
        f.put("fieldType", type);
        f.put("fieldOrder", order);
        f.put("required", required);
        f.put("fkEntityCode", fkEntityCode);
        f.put("validatorPresets", presets);
        return f;
    }
}
