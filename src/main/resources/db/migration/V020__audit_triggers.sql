-- =============================================================================
-- Person V020 — Audit log triggers
-- =============================================================================
-- PL/pgSQL function reusable + CREATE TRIGGER en las tablas principales.
-- Reusa el patrón compliance-grade de manufacturing / inventory.
--
--   AFTER INSERT  -> action='create', new=row,  old=null
--   AFTER UPDATE  -> action='update', new=NEW,  old=OLD
--   AFTER DELETE  -> action='delete', new=null, old=OLD
--   changed_by_id = current_setting('app.current_user_id', true)::uuid
--   tenant_id     = NEW/OLD.tenant_id
-- =============================================================================

-- Tabla de audit log
CREATE TABLE person.per_evt_auditlog_tbl (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id           UUID,
    entity_type         TEXT        NOT NULL,
    entity_id           UUID        NOT NULL,
    action              TEXT        NOT NULL,
    old_values_jsonb    JSONB,
    new_values_jsonb    JSONB,
    changed_by_id       UUID,
    changed_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- Campos estándar IMAP
    state_id            UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by_id       UUID,
    updated_by_id       UUID,
    CONSTRAINT per_evt_auditlog_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_per_auditlog_entity ON person.per_evt_auditlog_tbl(entity_type, entity_id);
CREATE INDEX idx_per_auditlog_tenant ON person.per_evt_auditlog_tbl(tenant_id, changed_at DESC);

-- Función de trigger
CREATE OR REPLACE FUNCTION person.audit_log_changes() RETURNS TRIGGER AS $$
DECLARE
    v_action      TEXT;
    v_old_jsonb   JSONB;
    v_new_jsonb   JSONB;
    v_entity_id   UUID;
    v_tenant_id   UUID;
    v_state_id    UUID;
    v_changed_by  UUID;
BEGIN
    IF (TG_OP = 'INSERT') THEN
        v_action    := 'create';
        v_old_jsonb := NULL;
        v_new_jsonb := to_jsonb(NEW);
        v_entity_id := NEW.id;
        v_tenant_id := NEW.tenant_id;
    ELSIF (TG_OP = 'UPDATE') THEN
        v_action    := 'update';
        v_old_jsonb := to_jsonb(OLD);
        v_new_jsonb := to_jsonb(NEW);
        v_entity_id := NEW.id;
        v_tenant_id := NEW.tenant_id;
    ELSE -- DELETE
        v_action    := 'delete';
        v_old_jsonb := to_jsonb(OLD);
        v_new_jsonb := NULL;
        v_entity_id := OLD.id;
        v_tenant_id := OLD.tenant_id;
    END IF;

    SELECT id INTO v_state_id FROM system.sys_state_tbl WHERE code = 'active' LIMIT 1;

    BEGIN
        v_changed_by := current_setting('app.current_user_id', true)::uuid;
    EXCEPTION WHEN OTHERS THEN
        v_changed_by := NULL;
    END;

    INSERT INTO person.per_evt_auditlog_tbl (
        id, tenant_id, entity_type, entity_id, action,
        old_values_jsonb, new_values_jsonb,
        changed_by_id, changed_at,
        state_id, created_at, updated_at, created_by_id, updated_by_id
    ) VALUES (
        gen_random_uuid(), v_tenant_id, TG_TABLE_NAME, v_entity_id, v_action,
        v_old_jsonb, v_new_jsonb,
        v_changed_by, NOW(),
        v_state_id, NOW(), NOW(), v_changed_by, v_changed_by
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION person.audit_log_changes() IS
    'Trigger function reusable: emite audit row a per_evt_auditlog_tbl on INSERT/UPDATE/DELETE. Lee app.current_user_id desde session (TenantContextService lo setea).';

-- Crear triggers en las tablas principales
DO $audit_setup$
DECLARE
    v_table TEXT;
    v_tables TEXT[] := ARRAY[
        'per_person_tbl',
        'per_tax_id_tbl',
        'per_address_tbl',
        'per_contact_tbl',
        'per_bank_account_tbl',
        'per_natural_detail_tbl',
        'per_legal_detail_tbl',
        'per_fiscal_profile_tbl',
        'per_fiscal_activity_tbl',
        'per_authority_tbl',
        'per_equity_holder_tbl',
        'per_relationship_tbl',
        'per_iam_link_tbl'
    ];
BEGIN
    FOREACH v_table IN ARRAY v_tables LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS trg_audit_log ON person.%I', v_table);
        EXECUTE format(
            'CREATE TRIGGER trg_audit_log AFTER INSERT OR UPDATE OR DELETE ON person.%I ' ||
            'FOR EACH ROW EXECUTE FUNCTION person.audit_log_changes()',
            v_table
        );
    END LOOP;
    RAISE NOTICE 'Audit triggers creados en % tablas', array_length(v_tables, 1);
END $audit_setup$;
