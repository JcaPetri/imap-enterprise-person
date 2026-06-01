package com.imap.person.infrastructure.adapter.out.persistence;

import com.imap.person.domain.port.out.PersonRepositoryPort;
import com.imap.person.infrastructure.entity.PerPersonEntity;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PersonRepositoryAdapter implements PersonRepositoryPort {

    private final PerPersonJpaRepository repo;

    public PersonRepositoryAdapter(PerPersonJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public PerPersonEntity save(PerPersonEntity entity) {
        return repo.save(entity);
    }

    @Override
    public Optional<PerPersonEntity> findById(UUID id) {
        return repo.findById(id);
    }

    @Override
    public List<PerPersonEntity> findByTenantId(UUID tenantId) {
        return repo.findByTenantIdAndActiveTrue(tenantId);
    }

    @Override
    public List<PerPersonEntity> searchByName(String query, UUID tenantId) {
        return repo.searchByName(query, tenantId);
    }
}
