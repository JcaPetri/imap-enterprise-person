-- =============================================================================
-- Person V007 — per_legal_detail_tbl (datos de persona jurídica)
-- =============================================================================

CREATE TABLE person.per_legal_detail_tbl (
    person_id                UUID,
    entity_type_id           UUID,       -- FK soft → per_dataelement_tbl WHERE category='ENTITY_TYPE'
    constitution_date        DATE,
    registration_body_id     UUID,       -- FK soft → per_dataelement_tbl WHERE category='REGISTRATION_BODY'
    registration_number      VARCHAR(50),
    registration_date        DATE,
    capital_currency_id      UUID,       -- FK soft → system.currency
    capital_suscripto        NUMERIC(20,2),
    capital_integrado        NUMERIC(20,2),
    shares_count             BIGINT,
    share_nominal_value      NUMERIC(12,4),
    exercise_end_month       SMALLINT    CHECK (exercise_end_month BETWEEN 1 AND 12),
    exercise_end_day         SMALLINT    CHECK (exercise_end_day   BETWEEN 1 AND 31),
    objeto_social            TEXT,
    audit_firm_id            UUID,       -- FK soft → per_person_tbl (la firma auditora)
    -- Campos estándar IMAP
    state_id                 UUID,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_id            UUID,
    updated_by_id            UUID,
    owned_by_id              UUID,
    timezone_id              UUID,
    table_history            TEXT,
    data_language_id         UUID,
    CONSTRAINT per_legal_detail_pkey      PRIMARY KEY (person_id),
    CONSTRAINT per_legal_detail_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id)
);
