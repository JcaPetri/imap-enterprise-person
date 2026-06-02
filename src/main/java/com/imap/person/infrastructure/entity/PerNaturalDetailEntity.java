package com.imap.person.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 1:1 with PerPersonEntity. PK = person_id (no separate id column).
 * JPA save() calls em.merge() because personId is never null → acts as upsert.
 */
@Entity
@Table(name = "per_natural_detail_tbl", schema = "person")
public class PerNaturalDetailEntity {

    @Id
    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place", length = 100)
    private String birthPlace;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "profession", length = 150)
    private String profession;

    @Column(name = "education_level", length = 20)
    private String educationLevel;

    @Column(name = "nationality_code", length = 3)
    private String nationalityCode;

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

    public PerNaturalDetailEntity() {}

    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getNationalityCode() { return nationalityCode; }
    public void setNationalityCode(String nationalityCode) { this.nationalityCode = nationalityCode; }
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
