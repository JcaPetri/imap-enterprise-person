-- =============================================================================
-- Person V008 — per_address_tbl (domicilios con soporte PostGIS)
-- =============================================================================

CREATE TABLE person.per_address_tbl (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    person_id        UUID         NOT NULL,
    tenant_id        UUID,
    address_type_id  UUID,                 -- FK soft → per_dataelement_tbl WHERE category='ADDRESS_TYPE'
    street           VARCHAR(200),
    street_number    VARCHAR(20),
    floor            VARCHAR(20),
    apartment        VARCHAR(20),
    city             VARCHAR(100),
    province_id      UUID,                 -- FK soft → system.province
    country_id       UUID,                 -- FK soft → system.country
    postal_code      VARCHAR(20),
    location         GEOGRAPHY(POINT, 4326),
    valid_from       DATE,
    valid_to         DATE,
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
    CONSTRAINT per_address_pkey      PRIMARY KEY (id),
    CONSTRAINT per_address_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_address_person   ON person.per_address_tbl(person_id) WHERE is_active = true;
CREATE INDEX idx_per_address_location ON person.per_address_tbl USING GIST(location);
