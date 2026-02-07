ALTER TABLE horaire_special
ADD COLUMN IF NOT EXISTS duplique CHAR(1) NOT NULL DEFAULT 'N' CHECK (duplique IN ('Y', 'N'));

COMMENT ON COLUMN horaire_special.duplique IS 'Duplication flag: Y or N (not editable on screen)';
