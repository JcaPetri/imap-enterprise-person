-- =============================================================================
-- Person V015 — per_authority_tbl (autoridades de personas jurídicas)
-- =============================================================================

CREATE TABLE person.per_authority_tbl (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    entity_id           UUID        NOT NULL, -- la persona jurídica
    authority_id        UUID        NOT NULL, -- la persona física que ejerce el cargo
    authority_role_id   UUID        NOT NULL, -- FK soft → per_dataelement_tbl WHERE category='AUTHORITY_ROLE'
    appointment_date    DATE,
    expiry_date         DATE,
    resolution_number   VARCHAR(50),
    is_active           BOOLEAN     NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id            UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_id       UUID,
    updated_by_id       UUID,
    owned_by_id         UUID,
    timezone_id         UUID,
    table_history       TEXT,
    data_language_id    UUID,
    CONSTRAINT per_authority_pkey       PRIMARY KEY (id),
    CONSTRAINT per_authority_entity_fk  FOREIGN KEY (entity_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_authority_person_fk  FOREIGN KEY (authority_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_authority_entity ON person.per_authority_tbl(entity_id)    WHERE is_active = true;
CREATE INDEX idx_per_authority_person ON person.per_authority_tbl(authority_id) WHERE is_active = true;
