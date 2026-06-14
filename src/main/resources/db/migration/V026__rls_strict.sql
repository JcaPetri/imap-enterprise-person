-- =============================================================================
-- Person V026 — RLS ESTRICTA (S7 Fase B, auditoría 2026-06-12)
-- =============================================================================
-- Decisión 2026-06-14: política ESTRICTA (fail-closed) en todos los micros, en vez
-- de permisiva. Recrea per_tenant_isolation sobre cada tabla per_* con tenant_id
-- SACANDO la cláusula permisiva `current_setting IS NULL`:
--   * USING / WITH CHECK = (tenant_id IS NULL OR tenant_id = <tenant del GUC>)
--   * si la transacción NO setea app.current_tenant_id → no ve filas de tenant
--     (solo las de plataforma, tenant_id NULL). Falla CERRADO, no filtra.
-- nullif(...,'')::uuid evita el error de cast si el GUC viniera como cadena vacía.
--
-- Requiere que el runtime setee el GUC en cada transacción (aspecto TenantRlsAspect).
-- Es inerte mientras el micro conecte como admindb (superuser bypassa); se activa
-- al pasar al rol dedicado person_app (Fase B).

DO $$
DECLARE t text;
BEGIN
    FOR t IN SELECT tablename FROM pg_tables
             WHERE schemaname = 'person' AND tablename LIKE 'per_%' LOOP
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'person' AND table_name = t AND column_name = 'tenant_id') THEN
            EXECUTE format('ALTER TABLE person.%I ENABLE ROW LEVEL SECURITY', t);
            EXECUTE format('DROP POLICY IF EXISTS per_tenant_isolation ON person.%I', t);
            EXECUTE format(
                'CREATE POLICY per_tenant_isolation ON person.%I '
                || 'USING (tenant_id IS NULL '
                || '       OR tenant_id = nullif(current_setting(''app.current_tenant_id'', true), '''')::uuid) '
                || 'WITH CHECK (tenant_id IS NULL '
                || '       OR tenant_id = nullif(current_setting(''app.current_tenant_id'', true), '''')::uuid)', t);
        END IF;
    END LOOP;
END $$;
