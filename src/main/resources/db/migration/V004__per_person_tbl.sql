-- =============================================================================
-- Person V004 — per_person_tbl (entidad principal)
-- =============================================================================

CREATE TABLE person.per_person_tbl (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID,
    person_type      VARCHAR(10)  NOT NULL CHECK (person_type IN ('NATURAL', 'LEGAL')),
    country_id       UUID,                 -- FK soft a system.country
    legal_name       VARCHAR(200) NOT NULL,
    trade_name       VARCHAR(200),
    search_key       VARCHAR(200) GENERATED ALWAYS AS (LOWER(legal_name)) STORED,
    notes            TEXT,
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
    CONSTRAINT per_person_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_per_person_tenant     ON person.per_person_tbl(tenant_id) WHERE is_active = true;
CREATE INDEX idx_per_person_search_key ON person.per_person_tbl USING GIN(search_key gin_trgm_ops);
CREATE INDEX idx_per_person_country    ON person.per_person_tbl(country_id);
