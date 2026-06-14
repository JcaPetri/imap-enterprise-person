-- =============================================================================
-- Person V024 — Fix RLS (hallazgo S2 auditoría 2026-06-12)
-- =============================================================================
-- Dos defectos que dejaban la RLS de person INERTE:
--   1. Las policies de V019 leían el GUC 'app.tenant_id', pero el código
--      (TenantContextService) setea 'app.current_tenant_id' → nunca matcheaban.
--   2. Las 6 tablas con tenant_id agregadas en V021 (per_tax_id, per_natural_detail,
--      per_legal_detail, per_authority, per_equity_holder, per_iam_link) no tenían
--      ninguna policy.
--
-- Esta migración dropea TODAS las policies viejas del schema person y recrea una
-- policy uniforme y correcta sobre cada tabla per_* que tenga tenant_id, con el
-- GUC correcto y el patrón permisivo estándar de la plataforma (igual que
-- sale/V032 y commerce/V020): permisiva cuando el contexto no está seteado; las
-- filas de plataforma (tenant_id NULL, p.ej. per_dataelement) quedan visibles.
--
-- NOTA: la RLS sigue siendo defensa-en-profundidad mientras el app-user conecte
-- con un rol que la bypassa. La activación dura (rol no-superuser + FORCE ROW
-- LEVEL SECURITY + cableado de TenantContextService.setContext por transacción)
-- es el item S7 de la auditoría y se hará uniforme en toda la plataforma.

DO $$
DECLARE r record; t text;
BEGIN
    -- 1. Dropear todas las policies pre-existentes del schema person (las de V019,
    --    con nombres viejos y GUC equivocado).
    FOR r IN SELECT policyname, tablename FROM pg_policies WHERE schemaname = 'person' LOOP
        EXECUTE format('DROP POLICY IF EXISTS %I ON person.%I', r.policyname, r.tablename);
    END LOOP;

    -- 2. Habilitar RLS + policy uniforme en cada tabla per_* con columna tenant_id.
    FOR t IN SELECT tablename FROM pg_tables
             WHERE schemaname = 'person' AND tablename LIKE 'per_%' LOOP
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'person' AND table_name = t AND column_name = 'tenant_id') THEN
            EXECUTE format('ALTER TABLE person.%I ENABLE ROW LEVEL SECURITY', t);
            EXECUTE format('DROP POLICY IF EXISTS per_tenant_isolation ON person.%I', t);
            EXECUTE format(
                'CREATE POLICY per_tenant_isolation ON person.%I USING ('
                || 'tenant_id IS NULL '
                || 'OR current_setting(''app.current_tenant_id'', true) IS NULL '
                || 'OR current_setting(''app.current_tenant_id'', true) = '''' '
                || 'OR tenant_id = current_setting(''app.current_tenant_id'', true)::uuid)', t);
        END IF;
    END LOOP;
END $$;
