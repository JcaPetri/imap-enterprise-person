-- =============================================================================
-- Person V001 — Extensions and schema
-- =============================================================================
-- Extensions (idempotentes, si ya existen en la DB no fallan)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Schema
CREATE SCHEMA IF NOT EXISTS person;

-- Search path hint para el schema
SET search_path TO person, public;
