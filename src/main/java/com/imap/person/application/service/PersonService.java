package com.imap.person.application.service;

import com.imap.person.domain.dto.CreatePersonRequest;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.domain.dto.PersonSummaryDto;
import com.imap.person.domain.port.in.PersonUseCase;
import com.imap.person.domain.port.out.PersonRepositoryPort;
import com.imap.person.infrastructure.entity.PerPersonEntity;
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

    public PersonService(PersonRepositoryPort repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public PersonDto create(CreatePersonRequest request) {
        UUID tenantId = TenantContextHolder.get();
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
    public Optional<PersonDto> findById(UUID id) {
        return repo.findById(id).map(this::toDto);
    }

    @Override
    public List<PersonSummaryDto> search(String query) {
        UUID tenantId = TenantContextHolder.get();
        return repo.searchByName(query, tenantId)
            .stream().map(this::toSummary).toList();
    }

    @Override
    public List<PersonSummaryDto> findAll() {
        UUID tenantId = TenantContextHolder.get();
        return repo.findByTenantId(tenantId)
            .stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional
    public PersonDto deactivate(UUID id) {
        PerPersonEntity entity = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Person not found: " + id));
        entity.setActive(false);
        entity.setUpdatedAt(OffsetDateTime.now());
        return toDto(repo.save(entity));
    }

    @Override
    @Transactional
    public PersonDto update(UUID id, CreatePersonRequest request) {
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
