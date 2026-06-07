package com.imap.person.application.service;

import com.imap.person.domain.dto.NaturalDetailDto;
import com.imap.person.domain.dto.UpsertNaturalDetailRequest;
import com.imap.person.infrastructure.entity.PerNaturalDetailEntity;
import com.imap.person.infrastructure.repository.PerNaturalDetailJpaRepository;
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
public class NaturalDetailService {

    private final PerNaturalDetailJpaRepository detailRepo;
    private final PerPersonJpaRepository        personRepo;

    public NaturalDetailService(PerNaturalDetailJpaRepository detailRepo,
                                PerPersonJpaRepository personRepo) {
        this.detailRepo = detailRepo;
        this.personRepo = personRepo;
    }

    /** PUT semantics — creates or replaces the 1:1 detail row. */
    public NaturalDetailDto upsert(UUID personId, UpsertNaturalDetailRequest req) {
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        OffsetDateTime now = OffsetDateTime.now();
        PerNaturalDetailEntity e = detailRepo.findById(personId)
            .orElseGet(() -> {
                PerNaturalDetailEntity n = new PerNaturalDetailEntity();
                n.setPersonId(personId);
                n.setCreatedAt(now);
                return n;
            });

        e.setTenantId(TenantContextHolder.get());
        e.setBirthDate(req.birthDate());
        e.setBirthPlace(req.birthPlace());
        e.setGender(req.gender());
        e.setMaritalStatus(req.maritalStatus());
        e.setProfession(req.profession());
        e.setEducationLevel(req.educationLevel());
        e.setNationalityCode(req.nationalityCode());
        e.setUpdatedAt(now);

        return toDto(detailRepo.save(e));
    }

    @Transactional(readOnly = true)
    public Optional<NaturalDetailDto> findByPerson(UUID personId) {
        return detailRepo.findById(personId).map(this::toDto);
    }

    private NaturalDetailDto toDto(PerNaturalDetailEntity e) {
        return new NaturalDetailDto(
            e.getPersonId(), e.getBirthDate(), e.getBirthPlace(),
            e.getGender(), e.getMaritalStatus(), e.getProfession(),
            e.getEducationLevel(), e.getNationalityCode(),
            e.getCreatedAt(), e.getUpdatedAt());
    }
}
