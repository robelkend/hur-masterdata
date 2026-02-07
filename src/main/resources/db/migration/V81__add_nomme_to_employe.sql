ALTER TABLE employe
ADD COLUMN IF NOT EXISTS nomme CHAR(1) NOT NULL DEFAULT 'N' CHECK (nomme IN ('Y', 'N'));

COMMENT ON COLUMN employe.nomme IS 'Employee appointed status: Y or N (not editable on screen)';
