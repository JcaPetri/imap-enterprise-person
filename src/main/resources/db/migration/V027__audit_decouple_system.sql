-- =============================================================================
-- Person V027 — Epic F1: desacoplar el trigger de auditoría de `system` (2026-06-15)
-- =============================================================================
-- audit_log_changes() leía `system.sys_state_tbl` (estado 'active') cross-schema.
-- Esa era la ÚNICA dependencia SQL cross-schema de person → eliminarla deja a
-- person 100% DB-independiente (listo para base separada). state_id del audit log
-- queda NULL (metadata no esencial; igual que los micros nuevos self-contained).
-- Único cambio vs V023: `SELECT id INTO v_state_id FROM system.sys_state_tbl...`
-- reemplazado por `v_state_id := NULL;`.

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

    v_state_id := NULL;   -- Epic F1: ya no se lee system.sys_state_tbl

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
