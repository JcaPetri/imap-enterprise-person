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

package com.imap.person.application.service;

import com.imap.person.domain.dto.CreatePersonRequest;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.domain.dto.PersonSummaryDto;
import com.imap.person.domain.port.in.PersonUseCase;
import com.imap.person.domain.port.out.PersonRepositoryPort;
import com.imap.person.infrastructure.entity.PerPersonEntity;
import com.imap.person.infrastructure.tenant.TenantContextService;
import com.imap.platform.tenant.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersonService implements PersonUseCase {

    private final PersonRepositoryPort repo;
    private final TenantContextService tenantContext;

    public PersonService(PersonRepositoryPort repo, TenantContextService tenantContext) {
        this.repo = repo;
        this.tenantContext = tenantContext;
    }

    @Override
    @Transactional
    public PersonDto create(CreatePersonRequest request) {
        UUID tenantId = TenantContextHolder.get();
        tenantContext.setContext(tenantId);   // S7: setea el GUC RLS dentro de la tx
        PerPersonEntity entity = new PerPersonEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setPersonType(request.personType());
        entity.setLegalName(request.legalName());
        entity.setTradeName(request.tradeName());
        entity.setCountryId(request.countryId());
        entity.setActive(true);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        PerPersonEntity saved = repo.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDto> findById(UUID id) {
        tenantContext.setContext(TenantContextHolder.get());
        return repo.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonSummaryDto> search(String query) {
        UUID tenantId = TenantContextHolder.get();
        tenantContext.setContext(tenantId);
        return repo.searchByName(query, tenantId)
            .stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonSummaryDto> findAll() {
        UUID tenantId = TenantContextHolder.get();
        tenantContext.setContext(tenantId);
        return repo.findByTenantId(tenantId)
            .stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional
    public PersonDto deactivate(UUID id) {
        tenantContext.setContext(TenantContextHolder.get());
        PerPersonEntity entity = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Person not found: " + id));
        entity.setActive(false);
        entity.setUpdatedAt(OffsetDateTime.now());
        return toDto(repo.save(entity));
    }

    @Override
    @Transactional
    public PersonDto update(UUID id, CreatePersonRequest request) {
        tenantContext.setContext(TenantContextHolder.get());
        PerPersonEntity entity = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Person not found: " + id));
        entity.setPersonType(request.personType());
        entity.setLegalName(request.legalName());
        entity.setTradeName(request.tradeName());
        entity.setCountryId(request.countryId());
        entity.setUpdatedAt(OffsetDateTime.now());
        return toDto(repo.save(entity));
    }

    private PersonDto toDto(PerPersonEntity e) {
        return new PersonDto(e.getId(), e.getTenantId(), e.getPersonType(),
            e.getLegalName(), e.getTradeName(), e.isActive(),
            e.getCreatedAt(), e.getUpdatedAt(), e.getCountryId());
    }

    private PersonSummaryDto toSummary(PerPersonEntity e) {
        return new PersonSummaryDto(e.getId(), e.getPersonType(),
            e.getLegalName(), e.getTradeName(), null);
    }
}
