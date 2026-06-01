-- =============================================================================
-- Person V012 — per_fiscal_profile_tbl (posición fiscal por organismo)
-- =============================================================================

CREATE TABLE person.per_fiscal_profile_tbl (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    person_id        UUID        NOT NULL,
    tenant_id        UUID,
    organism_code    VARCHAR(20) NOT NULL, -- loose coupling: ARCA | IIBB_CBA | CEI_CBA | ...
    fiscal_position  VARCHAR(30) NOT NULL, -- RI | CF | EX | MT | NO_APLICA | GRAN_CONTRIBUYENTE
    registered_since DATE,
    registered_to    DATE,
    is_active        BOOLEAN     NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id         UUID,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_id    UUID,
    updated_by_id    UUID,
    owned_by_id      UUID,
    timezone_id      UUID,
    table_history    TEXT,
    data_language_id UUID,
    CONSTRAINT per_fiscal_profile_pkey      PRIMARY KEY (id),
    CONSTRAINT per_fiscal_profile_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_fiscal_profile_unique    UNIQUE (person_id, organism_code, fiscal_position)
);

CREATE INDEX idx_per_fiscal_profile_person   ON person.per_fiscal_profile_tbl(person_id) WHERE is_active = true;
CREATE INDEX idx_per_fiscal_profile_organism ON person.per_fiscal_profile_tbl(organism_code, fiscal_position);
