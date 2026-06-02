package com.imap.person.application.service;

import com.imap.person.domain.dto.CreateIamLinkRequest;
import com.imap.person.domain.dto.IamLinkDto;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.infrastructure.entity.PerIamLinkEntity;
import com.imap.person.infrastructure.entity.PerPersonEntity;
import com.imap.person.infrastructure.repository.PerIamLinkJpaRepository;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import com.imap.person.infrastructure.tenant.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IamLinkService {

    private final PerIamLinkJpaRepository iamLinkRepo;
    private final PerPersonJpaRepository  personRepo;

    public IamLinkService(PerIamLinkJpaRepository iamLinkRepo,
                          PerPersonJpaRepository personRepo) {
        this.iamLinkRepo = iamLinkRepo;
        this.personRepo  = personRepo;
    }

    public IamLinkDto create(UUID personId, CreateIamLinkRequest req) {
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        if (iamLinkRepo.existsByIamEntityTypeAndIamEntityId(
                req.iamEntityType(), req.iamEntityId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "IAM entity already linked: " + req.iamEntityType() + "/" + req.iamEntityId());
        }

        OffsetDateTime now = OffsetDateTime.now();
        PerIamLinkEntity e = new PerIamLinkEntity();
        e.setId(UUID.randomUUID());
        e.setPersonId(personId);
        e.setTenantId(TenantContextHolder.get());
        e.setIamEntityType(req.iamEntityType());
        e.setIamEntityId(req.iamEntityId());
        e.setPrimary(req.primary());
        e.setLinkedAt(now);
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(iamLinkRepo.save(e));
    }

    @Transactional(readOnly = true)
    public List<IamLinkDto> listByPerson(UUID personId) {
        return iamLinkRepo.findByPersonIdAndActiveTrue(personId)
            .stream().map(this::toDto).toList();
    }

    /** Reverse lookup: given IAM entity → return the linked Person. */
    @Transactional(readOnly = true)
    public PersonDto findPersonByIamEntity(String iamEntityType, UUID iamEntityId) {
        PerIamLinkEntity link = iamLinkRepo
            .findByIamEntityTypeAndIamEntityIdAndActiveTrue(iamEntityType, iamEntityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No active link for " + iamEntityType + "/" + iamEntityId));

        PerPersonEntity person = personRepo.findById(link.getPersonId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + link.getPersonId()));

        return toPersonDto(person);
    }

    private IamLinkDto toDto(PerIamLinkEntity e) {
        return new IamLinkDto(
            e.getId(), e.getPersonId(),
            e.getIamEntityType(), e.getIamEntityId(),
            e.isPrimary(), e.getLinkedAt(),
            e.isActive(), e.getCreatedAt());
    }

    private PersonDto toPersonDto(PerPersonEntity e) {
        return new PersonDto(
            e.getId(), e.getTenantId(), e.getPersonType(),
            e.getLegalName(), e.getTradeName(),
            e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
