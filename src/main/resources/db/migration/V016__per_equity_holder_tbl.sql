-- =============================================================================
-- Person V016 — per_equity_holder_tbl (accionistas / socios)
-- =============================================================================

CREATE TABLE person.per_equity_holder_tbl (
    id                UUID           NOT NULL DEFAULT gen_random_uuid(),
    entity_id         UUID           NOT NULL, -- la empresa
    holder_id         UUID           NOT NULL, -- el accionista/socio (persona física o jurídica)
    share_class_id    UUID,                    -- FK soft → per_dataelement_tbl WHERE category='SHARE_CLASS'
    shares_count      BIGINT,
    nominal_value     NUMERIC(12,4),
    currency_id       UUID,                    -- FK soft → system.currency
    equity_percentage NUMERIC(7,4),
    effective_from    DATE,
    effective_to      DATE,
    registration_ref  VARCHAR(100),
    is_active         BOOLEAN        NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id          UUID,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    created_by_id     UUID,
    updated_by_id     UUID,
    owned_by_id       UUID,
    timezone_id       UUID,
    table_history     TEXT,
    data_language_id  UUID,
    CONSTRAINT per_equity_holder_pkey   PRIMARY KEY (id),
    CONSTRAINT per_equity_entity_fk     FOREIGN KEY (entity_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_equity_holder_fk     FOREIGN KEY (holder_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_equity_entity ON person.per_equity_holder_tbl(entity_id) WHERE is_active = true;
CREATE INDEX idx_per_equity_holder ON person.per_equity_holder_tbl(holder_id) WHERE is_active = true;
