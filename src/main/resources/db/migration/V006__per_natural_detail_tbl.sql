-- =============================================================================
-- Person V006 — per_natural_detail_tbl (datos de persona física)
-- =============================================================================

CREATE TABLE person.per_natural_detail_tbl (
    person_id        UUID        NOT NULL,
    birth_date       DATE,
    birth_place      VARCHAR(100),
    gender           VARCHAR(10)  CHECK (gender IN ('M', 'F', 'X', 'NO_INFORMA')),
    marital_status   VARCHAR(20)  CHECK (marital_status IN ('SOLTERO','CASADO','DIVORCIADO','VIUDO','UNION_CIVIL','SEPARADO')),
    profession       VARCHAR(150),
    education_level  VARCHAR(20)  CHECK (education_level IN ('PRIMARIO','SECUNDARIO','TERCIARIO','UNIVERSITARIO','POSGRADO')),
    nationality_code CHAR(3),
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
    CONSTRAINT per_natural_detail_pkey    PRIMARY KEY (person_id),
    CONSTRAINT per_natural_detail_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id)
);
