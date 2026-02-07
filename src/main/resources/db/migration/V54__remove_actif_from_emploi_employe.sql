-- Migration: Remove 'actif' column from emploi_employe and update statut_emploi to include NOUVEAU
-- Date: 2026-01-10

-- First, update existing records: set statut_emploi to 'NOUVEAU' where actif = 'N' and statut_emploi is not 'SUSPENDU' or 'TERMINE'
UPDATE emploi_employe 
SET statut_emploi = 'NOUVEAU' 
WHERE (actif = 'N' OR actif IS NULL) 
  AND statut_emploi NOT IN ('SUSPENDU', 'TERMINE');

-- Update statut_emploi to 'ACTIF' where actif = 'Y' and statut_emploi is not 'SUSPENDU' or 'TERMINE'
UPDATE emploi_employe 
SET statut_emploi = 'ACTIF' 
WHERE actif = 'Y' 
  AND statut_emploi NOT IN ('SUSPENDU', 'TERMINE');

-- Drop the old CHECK constraint if it exists (may vary by database)
-- PostgreSQL
ALTER TABLE emploi_employe DROP CONSTRAINT IF EXISTS emploi_employe_statut_emploi_check;

-- Add the new CHECK constraint with NOUVEAU
ALTER TABLE emploi_employe 
ADD CONSTRAINT emploi_employe_statut_emploi_check 
CHECK (statut_emploi IN ('NOUVEAU', 'ACTIF', 'SUSPENDU', 'TERMINE'));

-- Set statut_emploi to NOT NULL with default 'NOUVEAU'
ALTER TABLE emploi_employe 
ALTER COLUMN statut_emploi SET DEFAULT 'NOUVEAU',
ALTER COLUMN statut_emploi SET NOT NULL;

-- Set default for new records if statut_emploi is NULL
UPDATE emploi_employe 
SET statut_emploi = 'NOUVEAU' 
WHERE statut_emploi IS NULL;

-- Drop the index on actif column before dropping the column
DROP INDEX IF EXISTS idx_emploi_actif;

-- Drop the actif column
ALTER TABLE emploi_employe DROP COLUMN IF EXISTS actif;
