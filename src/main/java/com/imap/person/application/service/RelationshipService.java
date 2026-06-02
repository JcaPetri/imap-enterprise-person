package com.imap.person.application.service;

import com.imap.person.domain.dto.CreateRelationshipRequest;
import com.imap.person.domain.dto.RelationshipDto;
import com.imap.person.infrastructure.entity.PerRelationshipEntity;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import com.imap.person.infrastructure.repository.PerRelationshipJpaRepository;
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
public class RelationshipService {

    private final PerRelationshipJpaRepository relRepo;
    private final PerPersonJpaRepository       personRepo;

    public RelationshipService(PerRelationshipJpaRepository relRepo,
                               PerPersonJpaRepository personRepo) {
        this.relRepo    = relRepo;
        this.personRepo = personRepo;
    }

    public RelationshipDto create(UUID fromPersonId, CreateRelationshipRequest req) {
        personRepo.findById(fromPersonId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + fromPersonId));
        personRepo.findById(req.toPersonId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Target person not found: " + req.toPersonId()));

        OffsetDateTime now = OffsetDateTime.now();
        PerRelationshipEntity e = new PerRelationshipEntity();
        e.setId(UUID.randomUUID());
        e.setTenantId(TenantContextHolder.get());
        e.setFromPersonId(fromPersonId);
        e.setToPersonId(req.toPersonId());
        e.setRelTypeId(req.relTypeId());
        e.setNotes(req.notes());
        e.setEffectiveFrom(req.effectiveFrom());
        e.setEffectiveTo(req.effectiveTo());
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(relRepo.save(e));
    }

    @Transactional(readOnly = true)
    public List<RelationshipDto> listByPerson(UUID fromPersonId) {
        return relRepo.findByFromPersonIdAndActiveTrue(fromPersonId)
            .stream().map(this::toDto).toList();
    }

    private RelationshipDto toDto(PerRelationshipEntity e) {
        return new RelationshipDto(
            e.getId(), e.getFromPersonId(), e.getToPersonId(),
            e.getRelTypeId(), e.getNotes(),
            e.getEffectiveFrom(), e.getEffectiveTo(),
            e.isActive(), e.getCreatedAt());
    }
}
