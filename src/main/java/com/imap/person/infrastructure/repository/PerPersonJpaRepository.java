package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerPersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface PerPersonJpaRepository extends JpaRepository<PerPersonEntity, UUID> {

    List<PerPersonEntity> findByTenantIdAndActiveTrue(UUID tenantId);

    @Query("""
        SELECT p FROM PerPersonEntity p
        WHERE p.tenantId = :tenantId
          AND p.active = true
          AND (
            FUNCTION('similarity', p.searchKey, :query) > 0.3
            OR LOWER(p.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY FUNCTION('similarity', p.searchKey, :query) DESC
        """)
    List<PerPersonEntity> searchByName(@Param("query") String query, @Param("tenantId") UUID tenantId);
}
