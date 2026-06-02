-- =============================================================================
-- Person V021 — Add missing tenant_id to tables covered by audit trigger
-- The audit_log_changes() function reads NEW.tenant_id on every table it
-- covers. Any table that lacks the column causes a PL/pgSQL error on INSERT.
-- =============================================================================

ALTER TABLE person.per_tax_id_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE person.per_natural_detail_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE person.per_legal_detail_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE person.per_authority_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE person.per_equity_holder_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

ALTER TABLE person.per_iam_link_tbl
    ADD COLUMN IF NOT EXISTS tenant_id UUID;
