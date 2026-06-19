-- =============================================================================
-- person V029 — Auditoría Doctrina D v2 (reemplazo total del audit v1)
-- =============================================================================
-- person ya tenía núcleo completo + audit v1 (audit_log_changes → per_evt_auditlog_tbl,
-- modelo entity_type/action/values_jsonb). Datos NO reales → reemplazo total por v2.
-- Nadie en Java lee la auditlog (solo la vista v_audit_recent, recreada acá).
--
-- Pasos: (1) drop audit v1 (vista + función CASCADE = triggers + tabla). (2) núcleo:
-- fn_fill_nucleo (jsonb) + completar cols + backfill + owned NOT NULL (ANTES del nuevo
-- audit → sin ruido). (3) audit v2: fn_audit_trigger + per_audit_log_tbl particionada +
-- append-only + cobertura + recrear v_audit_recent canónica.
-- admindb. PG 18.4.
-- =============================================================================
SET search_path TO person, public;

-- ─── 1. Drop audit v1 ────────────────────────────────────────────────────────────────────────
DROP VIEW     IF EXISTS person.v_audit_recent;
DROP FUNCTION IF EXISTS person.audit_log_changes() CASCADE;   -- CASCADE = drop triggers trg_audit_log
DROP TABLE    IF EXISTS person.per_evt_auditlog_tbl;

-- ─── 2. Núcleo: fn_fill_nucleo + completar/backfill ──────────────────────────────────────────
CREATE OR REPLACE FUNCTION person.fn_fill_nucleo()
RETURNS trigger LANGUAGE plpgsql AS $fn$
DECLARE
    g     uuid  := COALESCE(nullif(current_setting('app.current_user_id', true), '')::uuid,
                            '00000000-0000-0000-0000-000000000000');
    j     jsonb := to_jsonb(NEW);
    patch jsonb := '{}'::jsonb;
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF j ? 'created_by_id' AND j->>'created_by_id' IS NULL THEN patch := patch || jsonb_build_object('created_by_id', g); END IF;
        IF j ? 'created_at'    AND j->>'created_at'    IS NULL THEN patch := patch || jsonb_build_object('created_at', now()); END IF;
        IF j ? 'owned_by_id'   AND j->>'owned_by_id'   IS NULL THEN patch := patch || jsonb_build_object('owned_by_id', COALESCE(j->>'created_by_id', g::text)); END IF;
        IF j ? 'updated_at'    AND j->>'updated_at'    IS NULL THEN patch := patch || jsonb_build_object('updated_at', now()); END IF;
        IF j ? 'updated_by_id' AND j->>'updated_by_id' IS NULL THEN patch := patch || jsonb_build_object('updated_by_id', g); END IF;
    ELSIF TG_OP = 'UPDATE' THEN
        IF j ? 'updated_at'    THEN patch := patch || jsonb_build_object('updated_at', now()); END IF;
        IF j ? 'updated_by_id' THEN patch := patch || jsonb_build_object('updated_by_id', g); END IF;
    END IF;
    IF patch <> '{}'::jsonb THEN NEW := jsonb_populate_record(NEW, patch); END IF;
    RETURN NEW;
END;
$fn$;

DO $loop$
DECLARE t text;
BEGIN
    FOR t IN
        SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname='person' AND c.relkind='r' AND c.relname LIKE 'per_%'
          AND c.relname <> 'per_dataelement_tbl'
          AND c.relname NOT LIKE 'per_audit_log%' AND c.relname NOT LIKE 'per_evt_auditlog%'
          AND EXISTS (SELECT 1 FROM information_schema.columns x WHERE x.table_schema='person' AND x.table_name=c.relname AND x.column_name='tenant_id')
        ORDER BY c.relname
    LOOP
        EXECUTE format('ALTER TABLE person.%I ADD COLUMN IF NOT EXISTS state_id      uuid', t);
        EXECUTE format('ALTER TABLE person.%I ADD COLUMN IF NOT EXISTS created_by_id uuid', t);
        EXECUTE format('ALTER TABLE person.%I ADD COLUMN IF NOT EXISTS updated_by_id uuid', t);
        EXECUTE format('ALTER TABLE person.%I ADD COLUMN IF NOT EXISTS owned_by_id   uuid', t);
        EXECUTE format('ALTER TABLE person.%I ADD COLUMN IF NOT EXISTS created_at    timestamptz NOT NULL DEFAULT now()', t);
        EXECUTE format('ALTER TABLE person.%I ADD COLUMN IF NOT EXISTS updated_at    timestamptz', t);
        EXECUTE format('UPDATE person.%I SET owned_by_id = COALESCE(created_by_id, %L::uuid) WHERE owned_by_id IS NULL', t, '00000000-0000-0000-0000-000000000000');
        EXECUTE format('UPDATE person.%I SET updated_at = created_at WHERE updated_at IS NULL', t);
        EXECUTE format('DROP TRIGGER IF EXISTS trg_set_updated_at ON person.%I', t);
        EXECUTE format('DROP TRIGGER IF EXISTS trg_fill_nucleo ON person.%I', t);
        EXECUTE format('CREATE TRIGGER trg_fill_nucleo BEFORE INSERT OR UPDATE ON person.%I FOR EACH ROW EXECUTE FUNCTION person.fn_fill_nucleo()', t);
        EXECUTE format('ALTER TABLE person.%I ALTER COLUMN owned_by_id SET NOT NULL', t);
    END LOOP;
END $loop$;

-- ─── 3. Audit v2 ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION person.fn_audit_trigger()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $fn$
DECLARE
    v_old JSONB; v_new JSONB; v_old_diff JSONB; v_new_diff JSONB; v_user_id UUID;
    c_system CONSTANT UUID := '00000000-0000-0000-0000-000000000000';
BEGIN
    IF TG_OP = 'INSERT' THEN RETURN NEW; END IF;
    BEGIN v_user_id := nullif(current_setting('app.current_user_id', true), '')::UUID;
    EXCEPTION WHEN OTHERS THEN v_user_id := NULL; END;
    v_user_id := COALESCE(v_user_id, c_system);
    IF TG_OP = 'DELETE' THEN
        INSERT INTO person.per_audit_log_tbl (tenant_id, table_name, operation, row_id, old_data, new_data, changed_by_id)
        VALUES (OLD.tenant_id, TG_TABLE_NAME, 'D', OLD.id, to_jsonb(OLD), NULL, v_user_id);
        RETURN OLD;
    END IF;
    v_old := to_jsonb(OLD); v_new := to_jsonb(NEW);
    SELECT jsonb_object_agg(k, v_new -> k) INTO v_new_diff FROM jsonb_object_keys(v_new) AS k WHERE (v_new -> k) IS DISTINCT FROM (v_old -> k);
    IF v_new_diff IS NULL THEN RETURN NEW; END IF;
    SELECT jsonb_object_agg(k, v_old -> k) INTO v_old_diff FROM jsonb_object_keys(v_old) AS k WHERE (v_new -> k) IS DISTINCT FROM (v_old -> k);
    INSERT INTO person.per_audit_log_tbl (tenant_id, table_name, operation, row_id, old_data, new_data, changed_by_id)
    VALUES (NEW.tenant_id, TG_TABLE_NAME, 'U', NEW.id, v_old_diff, v_new_diff, v_user_id);
    RETURN NEW;
END;
$fn$;

CREATE TABLE person.per_audit_log_tbl (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id UUID,
    table_name VARCHAR(100) NOT NULL,
    operation CHAR(1) NOT NULL CHECK (operation IN ('U','D')),
    row_id UUID,
    old_data JSONB,
    new_data JSONB,
    changed_by_id UUID NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, changed_at)
) PARTITION BY RANGE (changed_at);

DO $part$
DECLARE d date := date '2026-06-01'; e date; nm text;
BEGIN
    WHILE d < date '2027-07-01' LOOP
        e := (d + interval '1 month')::date;
        nm := 'per_audit_log_' || to_char(d, 'YYYY_MM');
        EXECUTE format('CREATE TABLE IF NOT EXISTS person.%I PARTITION OF person.per_audit_log_tbl FOR VALUES FROM (%L) TO (%L)', nm, d, e);
        d := e;
    END LOOP;
END $part$;
CREATE TABLE IF NOT EXISTS person.per_audit_log_default PARTITION OF person.per_audit_log_tbl DEFAULT;

CREATE INDEX idx_per_audit_table_row ON person.per_audit_log_tbl (table_name, row_id, changed_at DESC);
CREATE INDEX idx_per_audit_tenant    ON person.per_audit_log_tbl (tenant_id, changed_at DESC) WHERE tenant_id IS NOT NULL;

ALTER TABLE person.per_audit_log_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_audit_isolation ON person.per_audit_log_tbl
    USING      (tenant_id IS NULL OR tenant_id = nullif(current_setting('app.current_tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id IS NULL OR tenant_id = nullif(current_setting('app.current_tenant_id', true), '')::uuid);

REVOKE ALL            ON person.per_audit_log_tbl FROM person_app;
GRANT  SELECT         ON person.per_audit_log_tbl TO   person_app;
REVOKE UPDATE, DELETE ON person.per_audit_log_tbl FROM PUBLIC;

CREATE OR REPLACE FUNCTION person.fn_audit_append_only()
RETURNS TRIGGER LANGUAGE plpgsql AS $ao$
BEGIN
    RAISE EXCEPTION 'person.per_audit_log_tbl es append-only (intento de % bloqueado)', TG_OP USING ERRCODE = 'check_violation';
END;
$ao$;
CREATE TRIGGER trg_audit_append_only BEFORE UPDATE OR DELETE ON person.per_audit_log_tbl FOR EACH ROW EXECUTE FUNCTION person.fn_audit_append_only();

DO $cov$
DECLARE t text;
BEGIN
    FOR t IN
        SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname='person' AND c.relkind='r' AND c.relname LIKE 'per_%'
          AND c.relname <> 'per_dataelement_tbl'
          AND c.relname NOT LIKE 'per_audit_log%' AND c.relname NOT LIKE 'per_evt_auditlog%'
          AND EXISTS (SELECT 1 FROM information_schema.columns x WHERE x.table_schema='person' AND x.table_name=c.relname AND x.column_name='id')
          AND EXISTS (SELECT 1 FROM information_schema.columns x WHERE x.table_schema='person' AND x.table_name=c.relname AND x.column_name='tenant_id')
        ORDER BY c.relname
    LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS trg_audit_%s ON person.%I', t, t);
        EXECUTE format('CREATE TRIGGER trg_audit_%s AFTER INSERT OR UPDATE OR DELETE ON person.%I FOR EACH ROW EXECUTE FUNCTION person.fn_audit_trigger()', t, t);
    END LOOP;
END $cov$;

-- recrear vista canónica (apunta al nuevo log; nadie la lee desde Java, pero se conserva)
CREATE VIEW person.v_audit_recent AS
SELECT a.id, a.tenant_id, a.table_name, a.row_id, a.operation, a.changed_by_id,
       u.email AS changed_by_email,
       COALESCE((u.first_name::text || ' '::text) || u.last_name::text, u.email::text) AS changed_by_name,
       a.changed_at, a.new_data, a.old_data
FROM person.per_audit_log_tbl a
LEFT JOIN iam_user_tbl u ON u.user_id::uuid = a.changed_by_id
WHERE a.changed_at > (now() - '30 days'::interval);

DO $$
DECLARE v_parts int; v_trg int; v_fill int;
BEGIN
    SELECT count(*) INTO v_parts FROM pg_inherits WHERE inhparent='person.per_audit_log_tbl'::regclass;
    SELECT count(DISTINCT c.relname) INTO v_trg FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='person' AND t.tgname LIKE 'trg_audit_%' AND NOT t.tgisinternal AND c.relkind='r' AND c.relname NOT LIKE 'per_audit_log%';
    SELECT count(*) INTO v_fill FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='person' AND t.tgname='trg_fill_nucleo' AND NOT t.tgisinternal;
    RAISE NOTICE '--- V029 — person audit v2: % part, % audit, % fill ---', v_parts, v_trg, v_fill;
END $$;
