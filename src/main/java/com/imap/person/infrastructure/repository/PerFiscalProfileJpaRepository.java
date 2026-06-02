package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerFiscalProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PerFiscalProfileJpaRepository extends JpaRepository<PerFiscalProfileEntity, UUID> {

    List<PerFiscalProfileEntity> findByPersonIdAndActiveTrue(UUID personId);
}
