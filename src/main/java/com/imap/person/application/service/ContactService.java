package com.imap.person.application.service;

import com.imap.person.domain.dto.ContactDto;
import com.imap.person.domain.dto.CreateContactRequest;
import com.imap.person.infrastructure.entity.PerContactEntity;
import com.imap.person.infrastructure.repository.PerContactJpaRepository;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
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
public class ContactService {

    private final PerContactJpaRepository contactRepo;
    private final PerPersonJpaRepository  personRepo;

    public ContactService(PerContactJpaRepository contactRepo,
                          PerPersonJpaRepository personRepo) {
        this.contactRepo = contactRepo;
        this.personRepo  = personRepo;
    }

    public ContactDto create(UUID personId, CreateContactRequest req) {
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        OffsetDateTime now = OffsetDateTime.now();
        PerContactEntity e = new PerContactEntity();
        e.setId(UUID.randomUUID());
        e.setPersonId(personId);
        e.setTenantId(TenantContextHolder.get());
        e.setContactTypeId(req.contactTypeId());
        e.setLabelId(req.labelId());
        e.setValue(req.value());
        e.setPrimary(req.primary());
        e.setValidated(false);
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(contactRepo.save(e));
    }

    @Transactional(readOnly = true)
    public List<ContactDto> listByPerson(UUID personId) {
        return contactRepo.findByPersonIdAndActiveTrue(personId)
            .stream().map(this::toDto).toList();
    }

    private ContactDto toDto(PerContactEntity e) {
        return new ContactDto(
            e.getId(), e.getPersonId(),
            e.getContactTypeId(), e.getLabelId(),
            e.getValue(), e.isPrimary(), e.isValidated(),
            e.isActive(), e.getCreatedAt());
    }
}
