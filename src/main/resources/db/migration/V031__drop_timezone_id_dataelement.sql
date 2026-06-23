-- =============================================================================
-- person — drop timezone_id de per_dataelement_tbl
-- =============================================================================
-- timezone_id es REDUNDANTE con timestamptz (que ya guarda el instante en UTC). La
-- tz de display es propiedad del usuario/tenant, no de cada celda EAV. Estaba 99.8% NULL.
-- data_language_id SE MANTIENE (es la clave i18n: idioma del string traducible). admindb.
-- =============================================================================
SET search_path TO person, public;

ALTER TABLE person.per_dataelement_tbl DROP COLUMN IF EXISTS timezone_id;

DO $$
DECLARE c int;
BEGIN
  SELECT count(*) INTO c FROM information_schema.columns
   WHERE table_schema='person' AND table_name='per_dataelement_tbl';
  RAISE NOTICE '--- per_dataelement_tbl sin timezone_id: % columnas ---', c;
END $$;
