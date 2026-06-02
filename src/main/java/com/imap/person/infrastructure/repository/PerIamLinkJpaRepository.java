package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerIamLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerIamLinkJpaRepository extends JpaRepository<PerIamLinkEntity, UUID> {

    List<PerIamLinkEntity> findByPersonIdAndActiveTrue(UUID personId);

    Optional<PerIamLinkEntity> findByIamEntityTypeAndIamEntityIdAndActiveTrue(
            String iamEntityType, UUID iamEntityId);

    boolean existsByIamEntityTypeAndIamEntityId(String iamEntityType, UUID iamEntityId);
}
