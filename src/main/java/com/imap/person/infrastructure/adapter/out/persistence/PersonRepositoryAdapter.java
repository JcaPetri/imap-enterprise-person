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
//  • [person] Master data de personas (federado por otros micros)
//  • [person] TaxId: is_current GENERATED ALWAYS
//  • [person] Lookup por CUIT; FiscalProfile ARCA/IIBB
// ─── GOLDEN-RULES:END ───

package com.imap.person.infrastructure.adapter.out.persistence;

import com.imap.person.domain.port.out.PersonRepositoryPort;
import com.imap.person.infrastructure.entity.PerPersonEntity;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PersonRepositoryAdapter implements PersonRepositoryPort {

    private final PerPersonJpaRepository repo;

    public PersonRepositoryAdapter(PerPersonJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public PerPersonEntity save(PerPersonEntity entity) {
        return repo.save(entity);
    }

    @Override
    public Optional<PerPersonEntity> findById(UUID id) {
        return repo.findById(id);
    }

    @Override
    public List<PerPersonEntity> findByTenantId(UUID tenantId) {
        return repo.findByTenantIdAndActiveTrue(tenantId);
    }

    @Override
    public List<PerPersonEntity> searchByName(String query, UUID tenantId) {
        return repo.searchByName(query, tenantId);
    }
}
