package com.imap.person.application.service;

import com.imap.person.infrastructure.entity.PerDataelementEntity;
import com.imap.person.infrastructure.repository.PerDataelementJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lectura de dataelements (catálogo EAV). Existe para que la query pase por un
 * método @Transactional bajo el pointcut del TenantRlsAspect (setea el GUC
 * app.current_tenant_id in-tx). El DataelementController NO debe llamar al repo
 * directo (doctrinas A y F de IMAP_GUIA_DESARROLLO.md §3.ter): se saltaría el
 * aspecto → query sin contexto de tenant (hoy benigno porque los dataelements
 * son tenant_id NULL/plataforma, pero rompe el aislamiento si hubiera por-tenant).
 */
@Service
public class DataelementService {

    private final PerDataelementJpaRepository repo;

    public DataelementService(PerDataelementJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<PerDataelementEntity> findByCategory(String category) {
        return repo.findByCategoryAndActiveTrue(category);
    }
}
