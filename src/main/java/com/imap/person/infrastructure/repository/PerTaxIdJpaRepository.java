package com.imap.person.infrastructure.repository;

import com.imap.person.infrastructure.entity.PerTaxIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerTaxIdJpaRepository extends JpaRepository<PerTaxIdEntity, UUID> {

    List<PerTaxIdEntity> findByPersonIdAndActiveTrue(UUID personId);

    @Query("SELECT t FROM PerTaxIdEntity t WHERE t.taxIdValue = :value AND t.current = true")
    Optional<PerTaxIdEntity> findCurrentByValue(@Param("value") String value);

    boolean existsByTaxIdValueAndCurrentTrue(String taxIdValue);
}
