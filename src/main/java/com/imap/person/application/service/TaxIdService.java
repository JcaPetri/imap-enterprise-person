package com.imap.person.application.service;

import com.imap.person.domain.dto.CreateTaxIdRequest;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.domain.dto.TaxIdDto;
import com.imap.person.infrastructure.entity.PerPersonEntity;
import com.imap.person.infrastructure.entity.PerTaxIdEntity;
import com.imap.person.infrastructure.repository.PerDocumentTypeJpaRepository;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import com.imap.person.infrastructure.repository.PerTaxIdJpaRepository;
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
public class TaxIdService {

    private final PerTaxIdJpaRepository      taxIdRepo;
    private final PerPersonJpaRepository     personRepo;
    private final PerDocumentTypeJpaRepository docTypeRepo;
    private final TenantContextService       tenantContext;

    public TaxIdService(PerTaxIdJpaRepository taxIdRepo,
                        PerPersonJpaRepository personRepo,
                        PerDocumentTypeJpaRepository docTypeRepo,
                        TenantContextService tenantContext) {
        this.taxIdRepo   = taxIdRepo;
        this.personRepo  = personRepo;
        this.docTypeRepo = docTypeRepo;
        this.tenantContext = tenantContext;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public TaxIdDto create(UUID personId, CreateTaxIdRequest req) {
        tenantContext.setContext(TenantContextHolder.get());
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        var docType = docTypeRepo.findByDataelementKey(req.documentTypeKey())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Unknown document type key: " + req.documentTypeKey()));

        if (taxIdRepo.existsByDocumentTypeIdAndTaxIdValueAndCurrentTrue(
                docType.getId(), req.taxIdValue())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Active tax ID already exists: " + req.documentTypeKey() + "/" + req.taxIdValue());
        }

        OffsetDateTime now = OffsetDateTime.now();
        PerTaxIdEntity e = new PerTaxIdEntity();
        e.setId(UUID.randomUUID());
        e.setPersonId(personId);
        e.setTenantId(TenantContextHolder.get());
        e.setDocumentTypeId(docType.getId());
        e.setTaxIdValue(req.taxIdValue());
        e.setCountryId(req.countryId());
        e.setValidFrom(req.validFrom());
        // valid_to = null → is_current = true (GENERATED ALWAYS AS valid_to IS NULL)
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(taxIdRepo.save(e), req.documentTypeKey());
    }

    // ── List by person ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaxIdDto> listByPerson(UUID personId) {
        tenantContext.setContext(TenantContextHolder.get());
        return taxIdRepo.findByPersonIdAndActiveTrue(personId)
            .stream().map(t -> toDto(t, null)).toList();
    }

    // ── Find person by CUIT ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PersonDto findByCuit(String cuit) {
        tenantContext.setContext(TenantContextHolder.get());
        var taxId = taxIdRepo.findCurrentByDocTypeKeyAndValue("CUIT", cuit)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No active CUIT found: " + cuit));
        var person = personRepo.findById(taxId.getPersonId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found for CUIT: " + cuit));
        return toPersonDto(person);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TaxIdDto toDto(PerTaxIdEntity e, String docTypeKey) {
        // is_current is a GENERATED ALWAYS column; derive it client-side
        // to avoid stale Java field value after INSERT before DB refresh.
        boolean current = e.getValidTo() == null;
        return new TaxIdDto(
            e.getId(), e.getPersonId(), e.getDocumentTypeId(),
            docTypeKey, e.getTaxIdValue(),
            e.getValidFrom(), e.getValidTo(),
            current, e.isActive(), e.getCreatedAt());
    }

    private PersonDto toPersonDto(PerPersonEntity e) {
        return new PersonDto(
            e.getId(), e.getTenantId(), e.getPersonType(),
            e.getLegalName(), e.getTradeName(), e.isActive(),
            e.getCreatedAt(), e.getUpdatedAt(), e.getCountryId());
    }
}
