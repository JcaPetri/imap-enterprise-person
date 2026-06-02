package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerDocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerDocumentTypeJpaRepository extends JpaRepository<PerDocumentTypeEntity, UUID> {

    Optional<PerDocumentTypeEntity> findByDataelementKey(String dataelementKey);

    List<PerDocumentTypeEntity> findByActiveTrue();
}
