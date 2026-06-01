-- =============================================================================
-- Person V003 — per_document_type_tbl + seeds
-- =============================================================================

CREATE TABLE person.per_document_type_tbl (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id             UUID,
    country_id            UUID,                 -- FK soft a system.country
    dataelement_key       VARCHAR(20)  NOT NULL,
    label_es              VARCHAR(100) NOT NULL,
    label_en              VARCHAR(100),
    format_mask           VARCHAR(50),
    is_unique_per_person  BOOLEAN      NOT NULL DEFAULT true,
    is_fiscal             BOOLEAN      NOT NULL DEFAULT false,
    sort_order            SMALLINT              DEFAULT 0,
    is_active             BOOLEAN      NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id              UUID,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by_id         UUID,
    updated_by_id         UUID,
    owned_by_id           UUID,
    timezone_id           UUID,
    table_history         TEXT,
    data_language_id      UUID,
    CONSTRAINT per_document_type_pkey PRIMARY KEY (id),
    CONSTRAINT per_document_type_key_uq UNIQUE (dataelement_key, country_id)
);

CREATE INDEX idx_per_document_type_key ON person.per_document_type_tbl(dataelement_key);

-- Seeds para Argentina (country_id NULL = aplica a todos)
INSERT INTO person.per_document_type_tbl
    (dataelement_key, label_es, label_en, format_mask, is_unique_per_person, is_fiscal, sort_order)
VALUES
    ('CUIT',      'CUIT (Argentina)',    'CUIT',        '##-########-#',    true,  true,   1),
    ('CUIL',      'CUIL (Argentina)',    'CUIL',        '##-########-#',    true,  false,  2),
    ('CDI',       'CDI (Argentina)',     'CDI',         '##-########-#',    true,  true,   3),
    ('DNI',       'DNI (Argentina)',     'DNI',         '########',         true,  false,  4),
    ('PASSPORT',  'Pasaporte',           'Passport',    NULL,               false, false,  5),
    ('CPF',       'CPF (Brasil)',        'CPF',         '###.###.###-##',   true,  true,   6),
    ('CNPJ',      'CNPJ (Brasil)',       'CNPJ',        '##.###.###/####-##',true, true,   7),
    ('RUT',       'RUT (Chile)',         'RUT',         '########-#',       true,  true,   8),
    ('RFC',       'RFC (México)',        'RFC',         NULL,               true,  true,   9),
    ('NIF',       'NIF (España)',        'NIF',         NULL,               true,  true,  10),
    ('EXTRANJERO','Doc. Extranjero',     'Foreign ID',  NULL,               false, false, 11);
