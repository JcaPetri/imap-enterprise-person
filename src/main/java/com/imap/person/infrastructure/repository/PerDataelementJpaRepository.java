package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerDataelementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PerDataelementJpaRepository extends JpaRepository<PerDataelementEntity, UUID> {

    List<PerDataelementEntity> findByCategoryAndActiveTrue(String category);

    List<PerDataelementEntity> findByTenantIdIsNullAndCategoryAndActiveTrue(String category);
}
