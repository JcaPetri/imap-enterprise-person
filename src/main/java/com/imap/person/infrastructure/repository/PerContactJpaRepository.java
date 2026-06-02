package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PerContactJpaRepository extends JpaRepository<PerContactEntity, UUID> {

    List<PerContactEntity> findByPersonIdAndActiveTrue(UUID personId);
}
