package com.imap.person.application.service;

import com.imap.person.domain.dto.BankAccountDto;
import com.imap.person.domain.dto.CreateBankAccountRequest;
import com.imap.person.infrastructure.entity.PerBankAccountEntity;
import com.imap.person.infrastructure.repository.PerBankAccountJpaRepository;
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
public class BankAccountService {

    private final PerBankAccountJpaRepository bankRepo;
    private final PerPersonJpaRepository      personRepo;

    public BankAccountService(PerBankAccountJpaRepository bankRepo,
                              PerPersonJpaRepository personRepo) {
        this.bankRepo   = bankRepo;
        this.personRepo = personRepo;
    }

    public BankAccountDto create(UUID personId, CreateBankAccountRequest req) {
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        OffsetDateTime now = OffsetDateTime.now();
        PerBankAccountEntity e = new PerBankAccountEntity();
        e.setId(UUID.randomUUID());
        e.setPersonId(personId);
        e.setTenantId(TenantContextHolder.get());
        e.setBankCodeId(req.bankCodeId());
        e.setAccountTypeId(req.accountTypeId());
        e.setCurrencyId(req.currencyId());
        e.setCbu(req.cbu());
        e.setCvu(req.cvu());
        e.setAlias(req.alias());
        e.setPrimary(req.primary());
        e.setValidFrom(req.validFrom());
        e.setValidTo(req.validTo());
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(bankRepo.save(e));
    }

    @Transactional(readOnly = true)
    public List<BankAccountDto> listByPerson(UUID personId) {
        return bankRepo.findByPersonIdAndActiveTrue(personId)
            .stream().map(this::toDto).toList();
    }

    private BankAccountDto toDto(PerBankAccountEntity e) {
        return new BankAccountDto(
            e.getId(), e.getPersonId(),
            e.getBankCodeId(), e.getAccountTypeId(), e.getCurrencyId(),
            e.getCbu(), e.getCvu(), e.getAlias(),
            e.isPrimary(), e.isActive(),
            e.getValidFrom(), e.getValidTo(), e.getCreatedAt());
    }
}
