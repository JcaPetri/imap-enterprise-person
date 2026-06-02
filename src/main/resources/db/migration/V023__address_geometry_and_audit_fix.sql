-- V023: (1) location geography → geometry para Hibernate Spatial 6
--       (2) audit_log_changes() robusto ante tipos sin cast a jsonb
--
-- geography(POINT,4326) y geometry(POINT,4326) son equivalentes para
-- distancias cortas (< 100 km); geometry tiene mejor soporte en Hibernate 6.
-- No hay filas en per_address_tbl → ALTER TABLE es instantáneo.

SET search_path = person, public;

-- ── 1. Convertir location de geography a geometry ────────────────────────────

ALTER TABLE per_address_tbl
    ALTER COLUMN location TYPE geometry(POINT,4326)
    USING CASE WHEN location IS NULL THEN NULL ELSE location::geometry END;

DROP INDEX IF EXISTS idx_per_address_location;
CREATE INDEX idx_per_address_location ON per_address_tbl USING GIST(location);

-- ── 2. Trigger audit robusto a tipos no serializables (geometry, etc.) ────────

CREATE OR REPLACE FUNCTION person.audit_log_changes()
RETURNS TRIGGER AS $$
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
        BEGIN
            v_new_jsonb := to_jsonb(NEW);
        EXCEPTION WHEN OTHERS THEN
            v_new_jsonb := jsonb_build_object('_error', SQLERRM, '_table', TG_TABLE_NAME);
        END;
        v_entity_id := NEW.id;
        v_tenant_id := NEW.tenant_id;

    ELSIF (TG_OP = 'UPDATE') THEN
        v_action    := 'update';
        BEGIN
            v_old_jsonb := to_jsonb(OLD);
        EXCEPTION WHEN OTHERS THEN
            v_old_jsonb := jsonb_build_object('_error', SQLERRM, '_table', TG_TABLE_NAME);
        END;
        BEGIN
            v_new_jsonb := to_jsonb(NEW);
        EXCEPTION WHEN OTHERS THEN
            v_new_jsonb := jsonb_build_object('_error', SQLERRM, '_table', TG_TABLE_NAME);
        END;
        v_entity_id := NEW.id;
        v_tenant_id := NEW.tenant_id;

    ELSE -- DELETE
        v_action    := 'delete';
        BEGIN
            v_old_jsonb := to_jsonb(OLD);
        EXCEPTION WHEN OTHERS THEN
            v_old_jsonb := jsonb_build_object('_error', SQLERRM, '_table', TG_TABLE_NAME);
        END;
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
    'Audit trigger function. Handles non-jsonb-castable column types (geometry, etc.) gracefully via EXCEPTION block.';
