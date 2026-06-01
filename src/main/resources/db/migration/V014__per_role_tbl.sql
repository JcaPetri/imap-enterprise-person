-- =============================================================================
-- Person V014 — per_role_tbl (roles de la persona en contextos externos)
-- =============================================================================

CREATE TABLE person.per_role_tbl (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    person_id        UUID        NOT NULL,
    tenant_id        UUID,
    role_type_id     UUID        NOT NULL, -- FK soft → sys_person_role_tbl en System microservice
    context_id       UUID,                 -- ID del contexto (empresa para DIRECTOR, etc.)
    effective_from   DATE,
    effective_to     DATE,
    metadata_jsonb   JSONB,
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
    CONSTRAINT per_role_pkey      PRIMARY KEY (id),
    CONSTRAINT per_role_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id)
);

CREATE INDEX idx_per_role_person ON person.per_role_tbl(person_id)    WHERE is_active = true;
CREATE INDEX idx_per_role_type   ON person.per_role_tbl(role_type_id) WHERE is_active = true;
