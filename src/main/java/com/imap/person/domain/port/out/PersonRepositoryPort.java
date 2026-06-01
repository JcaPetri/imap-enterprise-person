package com.imap.person.domain.port.out;

import com.imap.person.infrastructure.entity.PerPersonEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepositoryPort {
    PerPersonEntity save(PerPersonEntity entity);
    Optional<PerPersonEntity> findById(UUID id);
    List<PerPersonEntity> findByTenantId(UUID tenantId);
    List<PerPersonEntity> searchByName(String query, UUID tenantId);
}
