-- Person V022 — add id column to 1:1 detail tables so audit trigger can read NEW.id
-- These tables use person_id as PK; audit_log_changes() requires NEW.id to exist.
ALTER TABLE person.per_natural_detail_tbl ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();
ALTER TABLE person.per_legal_detail_tbl   ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();
