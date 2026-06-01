-- =============================================================================
-- Person V017 — per_relationship_tbl (relaciones entre personas)
-- =============================================================================

CREATE TABLE person.per_relationship_tbl (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id       UUID,
    from_person_id  UUID        NOT NULL,
    to_person_id    UUID        NOT NULL,
    rel_type_id     UUID        NOT NULL, -- FK soft → per_dataelement_tbl WHERE category='RELATIONSHIP_TYPE'
    notes           TEXT,
    effective_from  DATE,
    effective_to    DATE,
    is_active       BOOLEAN     NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id        UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_id   UUID,
    updated_by_id   UUID,
    owned_by_id     UUID,
    timezone_id     UUID,
    table_history   TEXT,
    data_language_id UUID,
    CONSTRAINT per_relationship_pkey    PRIMARY KEY (id),
    CONSTRAINT per_rel_from_fk          FOREIGN KEY (from_person_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_rel_to_fk            FOREIGN KEY (to_person_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_rel_from ON person.per_relationship_tbl(from_person_id) WHERE is_active = true;
CREATE INDEX idx_per_rel_to   ON person.per_relationship_tbl(to_person_id)   WHERE is_active = true;
