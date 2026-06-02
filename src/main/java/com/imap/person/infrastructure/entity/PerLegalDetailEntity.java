package com.imap.person.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 1:1 with PerPersonEntity. PK = person_id (no separate id column).
 * JPA save() calls em.merge() because personId is never null → acts as upsert.
 */
@Entity
@Table(name = "per_legal_detail_tbl", schema = "person")
public class PerLegalDetailEntity {

    @Id
    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "entity_type_id")
    private UUID entityTypeId;

    @Column(name = "constitution_date")
    private LocalDate constitutionDate;

    @Column(name = "registration_body_id")
    private UUID registrationBodyId;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "capital_currency_id")
    private UUID capitalCurrencyId;

    @Column(name = "capital_suscripto", precision = 20, scale = 2)
    private BigDecimal capitalSuscripto;

    @Column(name = "capital_integrado", precision = 20, scale = 2)
    private BigDecimal capitalIntegrado;

    @Column(name = "shares_count")
    private Long sharesCount;

    @Column(name = "share_nominal_value", precision = 12, scale = 4)
    private BigDecimal shareNominalValue;

    @Column(name = "exercise_end_month")
    private Short exerciseEndMonth;

    @Column(name = "exercise_end_day")
    private Short exerciseEndDay;

    @Column(name = "objeto_social")
    private String objetoSocial;

    @Column(name = "audit_firm_id")
    private UUID auditFirmId;

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

    public PerLegalDetailEntity() {}

    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getEntityTypeId() { return entityTypeId; }
    public void setEntityTypeId(UUID entityTypeId) { this.entityTypeId = entityTypeId; }
    public LocalDate getConstitutionDate() { return constitutionDate; }
    public void setConstitutionDate(LocalDate constitutionDate) { this.constitutionDate = constitutionDate; }
    public UUID getRegistrationBodyId() { return registrationBodyId; }
    public void setRegistrationBodyId(UUID registrationBodyId) { this.registrationBodyId = registrationBodyId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public UUID getCapitalCurrencyId() { return capitalCurrencyId; }
    public void setCapitalCurrencyId(UUID capitalCurrencyId) { this.capitalCurrencyId = capitalCurrencyId; }
    public BigDecimal getCapitalSuscripto() { return capitalSuscripto; }
    public void setCapitalSuscripto(BigDecimal capitalSuscripto) { this.capitalSuscripto = capitalSuscripto; }
    public BigDecimal getCapitalIntegrado() { return capitalIntegrado; }
    public void setCapitalIntegrado(BigDecimal capitalIntegrado) { this.capitalIntegrado = capitalIntegrado; }
    public Long getSharesCount() { return sharesCount; }
    public void setSharesCount(Long sharesCount) { this.sharesCount = sharesCount; }
    public BigDecimal getShareNominalValue() { return shareNominalValue; }
    public void setShareNominalValue(BigDecimal shareNominalValue) { this.shareNominalValue = shareNominalValue; }
    public Short getExerciseEndMonth() { return exerciseEndMonth; }
    public void setExerciseEndMonth(Short exerciseEndMonth) { this.exerciseEndMonth = exerciseEndMonth; }
    public Short getExerciseEndDay() { return exerciseEndDay; }
    public void setExerciseEndDay(Short exerciseEndDay) { this.exerciseEndDay = exerciseEndDay; }
    public String getObjetoSocial() { return objetoSocial; }
    public void setObjetoSocial(String objetoSocial) { this.objetoSocial = objetoSocial; }
    public UUID getAuditFirmId() { return auditFirmId; }
    public void setAuditFirmId(UUID auditFirmId) { this.auditFirmId = auditFirmId; }
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
