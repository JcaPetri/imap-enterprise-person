-- =============================================================================
-- Person V009 — per_contact_tbl (medios de contacto)
-- =============================================================================

CREATE TABLE person.per_contact_tbl (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    person_id        UUID         NOT NULL,
    tenant_id        UUID,
    contact_type_id  UUID,                 -- FK soft → per_dataelement_tbl WHERE category='CONTACT_TYPE'
    label_id         UUID,                 -- FK soft → per_dataelement_tbl WHERE category='CONTACT_LABEL'
    value            VARCHAR(200) NOT NULL,
    is_primary       BOOLEAN      NOT NULL DEFAULT false,
    is_validated     BOOLEAN      NOT NULL DEFAULT false,
    validated_at     TIMESTAMPTZ,
    is_active        BOOLEAN      NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id         UUID,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by_id    UUID,
    updated_by_id    UUID,
    owned_by_id      UUID,
    timezone_id      UUID,
    table_history    TEXT,
    data_language_id UUID,
    CONSTRAINT per_contact_pkey      PRIMARY KEY (id),
    CONSTRAINT per_contact_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_contact_person ON person.per_contact_tbl(person_id) WHERE is_active = true;
