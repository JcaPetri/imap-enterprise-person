package com.imap.person.infrastructure.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "per_document_type_tbl", schema = "person")
public class PerDocumentTypeEntity {

    @Id @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "dataelement_key", nullable = false, length = 20)
    private String dataelementKey;

    @Column(name = "label_es", nullable = false, length = 100)
    private String labelEs;

    @Column(name = "label_en", length = 100)
    private String labelEn;

    @Column(name = "is_fiscal", nullable = false)
    private boolean fiscal = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public PerDocumentTypeEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getDataelementKey() { return dataelementKey; }
    public void setDataelementKey(String dataelementKey) { this.dataelementKey = dataelementKey; }
    public String getLabelEs() { return labelEs; }
    public void setLabelEs(String labelEs) { this.labelEs = labelEs; }
    public String getLabelEn() { return labelEn; }
    public void setLabelEn(String labelEn) { this.labelEn = labelEn; }
    public boolean isFiscal() { return fiscal; }
    public void setFiscal(boolean fiscal) { this.fiscal = fiscal; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
