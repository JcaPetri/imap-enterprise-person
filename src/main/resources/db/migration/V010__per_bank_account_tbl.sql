-- =============================================================================
-- Person V010 — per_bank_account_tbl (cuentas bancarias)
-- =============================================================================

CREATE TABLE person.per_bank_account_tbl (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    person_id        UUID        NOT NULL,
    tenant_id        UUID,
    bank_code_id     UUID,                -- FK soft → per_dataelement_tbl WHERE category='BANK'
    account_type_id  UUID,                -- FK soft → per_dataelement_tbl WHERE category='BANK_ACCOUNT_TYPE'
    currency_id      UUID,                -- FK soft → system.currency
    cbu              VARCHAR(22),
    cvu              VARCHAR(22),
    alias            VARCHAR(50),
    is_primary       BOOLEAN     NOT NULL DEFAULT false,
    is_active        BOOLEAN     NOT NULL DEFAULT true,
    valid_from       DATE,
    valid_to         DATE,
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
    CONSTRAINT per_bank_account_pkey      PRIMARY KEY (id),
    CONSTRAINT per_bank_account_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_bank_account_cbu_uq    UNIQUE (cbu),
    CONSTRAINT per_bank_account_cvu_uq    UNIQUE (cvu)
);

CREATE INDEX idx_per_bank_account_person ON person.per_bank_account_tbl(person_id) WHERE is_active = true;
