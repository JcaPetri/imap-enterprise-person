package com.imap.person.application.service;

import com.imap.person.domain.dto.CreateFiscalProfileRequest;
import com.imap.person.domain.dto.FiscalProfileDto;
import com.imap.person.infrastructure.entity.PerFiscalProfileEntity;
import com.imap.person.infrastructure.repository.PerFiscalProfileJpaRepository;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import com.imap.person.infrastructure.tenant.TenantContextService;
import com.imap.platform.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FiscalProfileService {

    private final PerFiscalProfileJpaRepository profileRepo;
    private final PerPersonJpaRepository        personRepo;
    private final TenantContextService          tenantContext;

    public FiscalProfileService(PerFiscalProfileJpaRepository profileRepo,
                                PerPersonJpaRepository personRepo,
                                TenantContextService tenantContext) {
        this.profileRepo = profileRepo;
        this.personRepo  = personRepo;
        this.tenantContext = tenantContext;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public FiscalProfileDto create(UUID personId, CreateFiscalProfileRequest req) {
        tenantContext.setContext(TenantContextHolder.get());
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        OffsetDateTime now = OffsetDateTime.now();
        PerFiscalProfileEntity e = new PerFiscalProfileEntity();
        e.setId(UUID.randomUUID());
        e.setPersonId(personId);
        e.setTenantId(TenantContextHolder.get());
        e.setOrganismCode(req.organismCode());
        e.setFiscalPosition(req.fiscalPosition());
        e.setRegisteredSince(req.registeredSince());
        e.setRegisteredTo(req.registeredTo());
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(profileRepo.save(e));
    }

    // ── List by person ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FiscalProfileDto> listByPerson(UUID personId) {
        tenantContext.setContext(TenantContextHolder.get());
        return profileRepo.findByPersonIdAndActiveTrue(personId)
            .stream().map(this::toDto).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private FiscalProfileDto toDto(PerFiscalProfileEntity e) {
        return new FiscalProfileDto(
            e.getId(), e.getPersonId(),
            e.getOrganismCode(), e.getFiscalPosition(),
            e.getRegisteredSince(), e.getRegisteredTo(),
            e.isActive(), e.getCreatedAt());
    }
}
