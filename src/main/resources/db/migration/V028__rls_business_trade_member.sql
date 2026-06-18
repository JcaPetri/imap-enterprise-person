-- =============================================================================
-- person V028 — Fase 1.3: tenant_id + RLS en per_business_trade_member_tbl
-- =============================================================================
-- Gap (auditoría / Fase 2): la tabla tiene datos de tenant pero NO tenía columna
-- `tenant_id` → RLS imposible (V026 la salteó: su DO-loop solo cubre tablas CON
-- tenant_id). Hoy la tabla está VACÍA (0 filas) y sin entidad/repo/service que la
-- escriba (definida en V011, nunca usada) → esto la deja RLS-ready para cuando se use.
--
-- tenant_id NOT NULL: los miembros de un business_trade son SIEMPRE tenant-scoped
-- (no hay miembros de plataforma). 0 filas → ADD NOT NULL sin default es válido.
-- Cuando aparezca un writer real, debe setear tenant_id desde TenantContextHolder
-- (patrón de AddressService/ContactService/etc.), si no el WITH CHECK lo frena.
--
-- Policy idéntica a V026 (per_tenant_isolation, fail-closed). Corre como admindb (DDL).
-- =============================================================================

ALTER TABLE person.per_business_trade_member_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL;

ALTER TABLE person.per_business_trade_member_tbl ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS per_tenant_isolation ON person.per_business_trade_member_tbl;
CREATE POLICY per_tenant_isolation ON person.per_business_trade_member_tbl
    USING (tenant_id IS NULL
           OR tenant_id = nullif(current_setting('app.current_tenant_id', true), '')::uuid)
    WITH CHECK (tenant_id IS NULL
           OR tenant_id = nullif(current_setting('app.current_tenant_id', true), '')::uuid);

DO $$
DECLARE v_rls boolean; v_pol int; v_col int;
BEGIN
    SELECT relrowsecurity INTO v_rls FROM pg_class
     WHERE oid = 'person.per_business_trade_member_tbl'::regclass;
    SELECT count(*) INTO v_pol FROM pg_policies
     WHERE schemaname='person' AND tablename='per_business_trade_member_tbl';
    SELECT count(*) INTO v_col FROM information_schema.columns
     WHERE table_schema='person' AND table_name='per_business_trade_member_tbl' AND column_name='tenant_id';
    RAISE NOTICE '--- V028 — tenant_id col=% · RLS=% · policies=% (esperado 1/true/1) ---', v_col, v_rls, v_pol;
END $$;
