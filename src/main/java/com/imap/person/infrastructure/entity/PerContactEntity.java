package com.imap.person.infrastructure.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "per_contact_tbl", schema = "person")
public class PerContactEntity {

    @Id @Column(name = "id")
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "contact_type_id")
    private UUID contactTypeId;

    @Column(name = "label_id")
    private UUID labelId;

    @Column(name = "value", nullable = false, length = 200)
    private String value;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "is_validated", nullable = false)
    private boolean validated = false;

    @Column(name = "validated_at")
    private OffsetDateTime validatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;

    @Column(name = "updated_by_id")
    private UUID updatedById;

    @Column(name = "owned_by_id")
    private UUID ownedById;

    @Column(name = "timezone_id")
    private UUID timezoneId;

    @Column(name = "table_history")
    private String tableHistory;

    @Column(name = "data_language_id")
    private UUID dataLanguageId;

    public PerContactEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getContactTypeId() { return contactTypeId; }
    public void setContactTypeId(UUID contactTypeId) { this.contactTypeId = contactTypeId; }
    public UUID getLabelId() { return labelId; }
    public void setLabelId(UUID labelId) { this.labelId = labelId; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
    public boolean isValidated() { return validated; }
    public void setValidated(boolean validated) { this.validated = validated; }
    public OffsetDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(OffsetDateTime validatedAt) { this.validatedAt = validatedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public UUID getStateId() { return stateId; }
    public void setStateId(UUID stateId) { this.stateId = stateId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getCreatedById() { return createdById; }
    public void setCreatedById(UUID createdById) { this.createdById = createdById; }
    public UUID getUpdatedById() { return updatedById; }
    public void setUpdatedById(UUID updatedById) { this.updatedById = updatedById; }
    public UUID getOwnedById() { return ownedById; }
    public void setOwnedById(UUID ownedById) { this.ownedById = ownedById; }
    public UUID getTimezoneId() { return timezoneId; }
    public void setTimezoneId(UUID timezoneId) { this.timezoneId = timezoneId; }
    public String getTableHistory() { return tableHistory; }
    public void setTableHistory(String tableHistory) { this.tableHistory = tableHistory; }
    public UUID getDataLanguageId() { return dataLanguageId; }
    public void setDataLanguageId(UUID dataLanguageId) { this.dataLanguageId = dataLanguageId; }
}
