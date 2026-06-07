package com.imap.person.application.service;

import com.imap.person.domain.dto.LegalDetailDto;
import com.imap.person.domain.dto.UpsertLegalDetailRequest;
import com.imap.person.infrastructure.entity.PerLegalDetailEntity;
import com.imap.person.infrastructure.repository.PerLegalDetailJpaRepository;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import com.imap.platform.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LegalDetailService {

    private final PerLegalDetailJpaRepository detailRepo;
    private final PerPersonJpaRepository      personRepo;

    public LegalDetailService(PerLegalDetailJpaRepository detailRepo,
                              PerPersonJpaRepository personRepo) {
        this.detailRepo = detailRepo;
        this.personRepo = personRepo;
    }

    /** PUT semantics — creates or replaces the 1:1 detail row. */
    public LegalDetailDto upsert(UUID personId, UpsertLegalDetailRequest req) {
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        OffsetDateTime now = OffsetDateTime.now();
        PerLegalDetailEntity e = detailRepo.findById(personId)
            .orElseGet(() -> {
                PerLegalDetailEntity n = new PerLegalDetailEntity();
                n.setPersonId(personId);
                n.setCreatedAt(now);
                return n;
            });

        e.setTenantId(TenantContextHolder.get());
        e.setEntityTypeId(req.entityTypeId());
        e.setConstitutionDate(req.constitutionDate());
        e.setRegistrationBodyId(req.registrationBodyId());
        e.setRegistrationNumber(req.registrationNumber());
        e.setRegistrationDate(req.registrationDate());
        e.setCapitalCurrencyId(req.capitalCurrencyId());
        e.setCapitalSuscripto(req.capitalSuscripto());
        e.setCapitalIntegrado(req.capitalIntegrado());
        e.setSharesCount(req.sharesCount());
        e.setShareNominalValue(req.shareNominalValue());
        e.setExerciseEndMonth(req.exerciseEndMonth() != null
            ? req.exerciseEndMonth().shortValue() : null);
        e.setExerciseEndDay(req.exerciseEndDay() != null
            ? req.exerciseEndDay().shortValue() : null);
        e.setObjetoSocial(req.objetoSocial());
        e.setAuditFirmId(req.auditFirmId());
        e.setUpdatedAt(now);

        return toDto(detailRepo.save(e));
    }

    @Transactional(readOnly = true)
    public Optional<LegalDetailDto> findByPerson(UUID personId) {
        return detailRepo.findById(personId).map(this::toDto);
    }

    private LegalDetailDto toDto(PerLegalDetailEntity e) {
        return new LegalDetailDto(
            e.getPersonId(), e.getEntityTypeId(),
            e.getConstitutionDate(), e.getRegistrationBodyId(),
            e.getRegistrationNumber(), e.getRegistrationDate(),
            e.getCapitalCurrencyId(), e.getCapitalSuscripto(), e.getCapitalIntegrado(),
            e.getSharesCount(), e.getShareNominalValue(),
            e.getExerciseEndMonth() != null ? e.getExerciseEndMonth().intValue() : null,
            e.getExerciseEndDay()   != null ? e.getExerciseEndDay().intValue()   : null,
            e.getObjetoSocial(), e.getAuditFirmId(),
            e.getCreatedAt(), e.getUpdatedAt());
    }
}
