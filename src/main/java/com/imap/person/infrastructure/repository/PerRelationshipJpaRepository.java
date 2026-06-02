package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PerRelationshipJpaRepository extends JpaRepository<PerRelationshipEntity, UUID> {

    List<PerRelationshipEntity> findByFromPersonIdAndActiveTrue(UUID fromPersonId);
}
