-- =============================================================================
-- Person V018 — per_iam_link_tbl (vínculo persona ↔ IAM tenant/user)
-- =============================================================================

CREATE TABLE person.per_iam_link_tbl (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    person_id        UUID        NOT NULL,
    iam_entity_type  VARCHAR(20) NOT NULL CHECK (iam_entity_type IN ('TENANT', 'USER')),
    iam_entity_id    UUID        NOT NULL,
    is_primary       BOOLEAN     NOT NULL DEFAULT false,
    linked_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
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
    CONSTRAINT per_iam_link_pkey      PRIMARY KEY (id),
    CONSTRAINT per_iam_link_person_fk FOREIGN KEY (person_id)
        REFERENCES person.per_person_tbl(id),
    CONSTRAINT per_iam_link_unique    UNIQUE (iam_entity_type, iam_entity_id)
);

CREATE INDEX idx_per_iam_link_person ON person.per_iam_link_tbl(person_id)      WHERE is_active = true;
CREATE INDEX idx_per_iam_link_entity ON person.per_iam_link_tbl(iam_entity_id);
