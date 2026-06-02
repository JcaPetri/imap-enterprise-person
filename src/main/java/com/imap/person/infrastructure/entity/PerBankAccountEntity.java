package com.imap.person.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "per_bank_account_tbl", schema = "person")
public class PerBankAccountEntity {

    @Id @Column(name = "id")
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "bank_code_id")
    private UUID bankCodeId;

    @Column(name = "account_type_id")
    private UUID accountTypeId;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Column(name = "cbu", length = 22)
    private String cbu;

    @Column(name = "cvu", length = 22)
    private String cvu;

    @Column(name = "alias", length = 50)
    private String alias;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

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

    public PerBankAccountEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getBankCodeId() { return bankCodeId; }
    public void setBankCodeId(UUID bankCodeId) { this.bankCodeId = bankCodeId; }
    public UUID getAccountTypeId() { return accountTypeId; }
    public void setAccountTypeId(UUID accountTypeId) { this.accountTypeId = accountTypeId; }
    public UUID getCurrencyId() { return currencyId; }
    public void setCurrencyId(UUID currencyId) { this.currencyId = currencyId; }
    public String getCbu() { return cbu; }
    public void setCbu(String cbu) { this.cbu = cbu; }
    public String getCvu() { return cvu; }
    public void setCvu(String cvu) { this.cvu = cvu; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
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
