-- Fix statut check constraint for supplementaire_employe and normalize existing data
ALTER TABLE supplementaire_employe
    DROP CONSTRAINT IF EXISTS supplementaire_employe_statut_check;

-- Normalize legacy status values
UPDATE supplementaire_employe
SET statut = 'BROUILLON'
WHERE statut IS NULL OR statut = 'NOUVEAU';

-- Ensure default is correct
ALTER TABLE supplementaire_employe
    ALTER COLUMN statut SET DEFAULT 'BROUILLON';

-- Recreate check constraint with valid values
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'supplementaire_employe_statut_check'
    ) THEN
        ALTER TABLE supplementaire_employe
            ADD CONSTRAINT supplementaire_employe_statut_check
            CHECK (statut IN ('BROUILLON', 'VALIDE'));
    END IF;
END $$;
