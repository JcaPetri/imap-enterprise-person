package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerNaturalDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/** PK is person_id (UUID). findById(personId) = lookup by person. */
public interface PerNaturalDetailJpaRepository extends JpaRepository<PerNaturalDetailEntity, UUID> {
}
