-- =============================================================================
-- Person V005 — per_tax_id_tbl (identificaciones fiscales)
-- =============================================================================

CREATE TABLE person.per_tax_id_tbl (
    id                UUID        NOT NULL DEFAULT gen_random_uuid(),
    person_id         UUID        NOT NULL,
    document_type_id  UUID        NOT NULL,
    tax_id_value      VARCHAR(30) NOT NULL,
    country_id        UUID,
    valid_from        DATE,
    valid_to          DATE,
    is_current        BOOLEAN     GENERATED ALWAYS AS (valid_to IS NULL) STORED,
    is_active         BOOLEAN     NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id          UUID,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_id     UUID,
    updated_by_id     UUID,
    owned_by_id       UUID,
    timezone_id       UUID,
    table_history     TEXT,
    data_language_id  UUID,
    CONSTRAINT per_tax_id_pkey PRIMARY KEY (id),
    CONSTRAINT per_tax_id_person_fk  FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_tax_id_doctype_fk FOREIGN KEY (document_type_id)
        REFERENCES person.per_document_type_tbl(id),
    CONSTRAINT per_tax_id_unique_current UNIQUE (document_type_id, tax_id_value, country_id)
);

CREATE INDEX idx_per_tax_id_person ON person.per_tax_id_tbl(person_id);
CREATE INDEX idx_per_tax_id_value  ON person.per_tax_id_tbl(tax_id_value) WHERE is_current = true;
