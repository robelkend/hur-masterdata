ALTER TABLE pointage_brut
    ADD COLUMN IF NOT EXISTS no_presence BIGINT;
