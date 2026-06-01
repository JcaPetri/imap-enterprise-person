package com.imap.person.infrastructure.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "per_dataelement_tbl", schema = "person")
public class PerDataelementEntity {

    @Id @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "label_es", nullable = false, length = 200)
    private String labelEs;

    @Column(name = "label_en", length = 200)
    private String labelEn;

    @Column(name = "label_pt", length = 200)
    private String labelPt;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "sort_order")
    private Short sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public PerDataelementEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabelEs() { return labelEs; }
    public void setLabelEs(String labelEs) { this.labelEs = labelEs; }
    public String getLabelEn() { return labelEn; }
    public void setLabelEn(String labelEn) { this.labelEn = labelEn; }
    public String getLabelPt() { return labelPt; }
    public void setLabelPt(String labelPt) { this.labelPt = labelPt; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public Short getSortOrder() { return sortOrder; }
    public void setSortOrder(Short sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
