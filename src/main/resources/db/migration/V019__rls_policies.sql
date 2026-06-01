-- =============================================================================
-- Person V019 — RLS policies (Row Level Security multi-tenant)
-- =============================================================================

-- per_person_tbl
ALTER TABLE person.per_person_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_person_tenant_isolation ON person.per_person_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_address_tbl
ALTER TABLE person.per_address_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_address_tenant_isolation ON person.per_address_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_contact_tbl
ALTER TABLE person.per_contact_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_contact_tenant_isolation ON person.per_contact_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_bank_account_tbl
ALTER TABLE person.per_bank_account_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_bank_account_tenant_isolation ON person.per_bank_account_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_business_trade_tbl (siempre tiene tenant_id — datos privados por tenant)
ALTER TABLE person.per_business_trade_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_business_trade_tenant_isolation ON person.per_business_trade_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- per_fiscal_profile_tbl
ALTER TABLE person.per_fiscal_profile_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_fiscal_profile_tenant_isolation ON person.per_fiscal_profile_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_fiscal_activity_tbl
ALTER TABLE person.per_fiscal_activity_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_fiscal_activity_tenant_isolation ON person.per_fiscal_activity_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_role_tbl
ALTER TABLE person.per_role_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_role_tenant_isolation ON person.per_role_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_relationship_tbl
ALTER TABLE person.per_relationship_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_relationship_tenant_isolation ON person.per_relationship_tbl
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid OR tenant_id IS NULL);

-- per_dataelement_tbl: datos de plataforma (tenant_id IS NULL) visibles para todos
ALTER TABLE person.per_dataelement_tbl ENABLE ROW LEVEL SECURITY;
CREATE POLICY per_dataelement_isolation ON person.per_dataelement_tbl
    USING (tenant_id IS NULL OR tenant_id = current_setting('app.tenant_id', true)::uuid);
