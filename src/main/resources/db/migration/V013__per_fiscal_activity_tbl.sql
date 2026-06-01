-- =============================================================================
-- Person V013 — per_fiscal_activity_tbl (actividades declaradas por organismo)
-- =============================================================================

CREATE TABLE person.per_fiscal_activity_tbl (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    person_id       UUID        NOT NULL,
    tenant_id       UUID,
    organism_code   VARCHAR(20) NOT NULL,
    activity_code   VARCHAR(20) NOT NULL,
    is_primary      BOOLEAN     NOT NULL DEFAULT false,
    declared_at     DATE,
    ceased_at       DATE,
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
    CONSTRAINT per_fiscal_activity_pkey      PRIMARY KEY (id),
    CONSTRAINT per_fiscal_activity_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_fiscal_activity_person ON person.per_fiscal_activity_tbl(person_id) WHERE is_active = true;
CREATE INDEX idx_per_fiscal_activity_code   ON person.per_fiscal_activity_tbl(organism_code, activity_code);
