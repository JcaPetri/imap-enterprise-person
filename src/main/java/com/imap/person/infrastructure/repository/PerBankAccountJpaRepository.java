package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerBankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PerBankAccountJpaRepository extends JpaRepository<PerBankAccountEntity, UUID> {

    List<PerBankAccountEntity> findByPersonIdAndActiveTrue(UUID personId);
}
