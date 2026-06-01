-- =============================================================================
-- Person V011 — per_business_trade_tbl + per_business_trade_member_tbl
-- =============================================================================

CREATE TABLE person.per_business_trade_tbl (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID         NOT NULL,
    trade_name       VARCHAR(200) NOT NULL,
    trade_type_id    UUID,                 -- FK soft → per_dataelement_tbl WHERE category='TRADE_TYPE'
    search_key       VARCHAR(200) GENERATED ALWAYS AS (LOWER(trade_name)) STORED,
    description      TEXT,
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
    CONSTRAINT per_business_trade_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_per_business_trade_tenant ON person.per_business_trade_tbl(tenant_id) WHERE is_active = true;
CREATE INDEX idx_per_business_trade_search ON person.per_business_trade_tbl USING GIN(search_key gin_trgm_ops);

CREATE TABLE person.per_business_trade_member_tbl (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    trade_id         UUID        NOT NULL,
    person_id        UUID        NOT NULL,
    is_default       BOOLEAN     NOT NULL DEFAULT false,
    display_label    VARCHAR(100),
    sort_order       SMALLINT             DEFAULT 0,
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
    CONSTRAINT per_business_trade_member_pkey PRIMARY KEY (id),
    CONSTRAINT per_btm_trade_fk  FOREIGN KEY (trade_id)
        REFERENCES person.per_business_trade_tbl(id),
    CONSTRAINT per_btm_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_btm_unique    UNIQUE (trade_id, person_id)
);

CREATE INDEX idx_per_btm_trade  ON person.per_business_trade_member_tbl(trade_id)  WHERE is_active = true;
CREATE INDEX idx_per_btm_person ON person.per_business_trade_member_tbl(person_id) WHERE is_active = true;
