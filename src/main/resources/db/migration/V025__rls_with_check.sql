-- =============================================================================
-- Person V025 — WITH CHECK en policies RLS (S7 paso 2, auditoría 2026-06-12)
-- =============================================================================
-- Las policies eran USING-only → aíslan la LECTURA pero no impiden ESCRIBIR filas
-- con tenant_id ajeno. Agrega WITH CHECK = (misma expresión USING) a cada policy
-- que no lo tenga, genéricamente vía pg_policies (cubre las policies arregladas en
-- V024). INERTE hasta FORCE (paso 4). Idempotente.

DO $$
DECLARE r RECORD;
BEGIN
    FOR r IN SELECT tablename, policyname, qual
             FROM pg_policies
             WHERE schemaname = 'person' AND with_check IS NULL AND qual IS NOT NULL
    LOOP
        EXECUTE format('ALTER POLICY %I ON person.%I WITH CHECK (%s)',
                       r.policyname, r.tablename, r.qual);
    END LOOP;
END $$;
