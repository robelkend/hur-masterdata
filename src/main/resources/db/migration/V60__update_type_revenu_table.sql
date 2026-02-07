-- Update type_revenu table with new fields
-- Add new columns
ALTER TABLE type_revenu
    ADD COLUMN IF NOT EXISTS entreprise_id BIGINT,
    ADD COLUMN IF NOT EXISTS categorie VARCHAR(30),
    ADD COLUMN IF NOT EXISTS imposable CHAR(1) DEFAULT 'Y' NOT NULL,
    ADD COLUMN IF NOT EXISTS soumis_cotisations CHAR(1) DEFAULT 'Y' NOT NULL,
    ADD COLUMN IF NOT EXISTS actif CHAR(1) DEFAULT 'Y' NOT NULL;

-- Add foreign key constraint for entreprise_id
ALTER TABLE type_revenu
    ADD CONSTRAINT fk_type_revenu_entreprise 
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL;

-- Update existing records to have default values for new NOT NULL columns
UPDATE type_revenu
SET categorie = 'AUTRE'
WHERE categorie IS NULL;

-- Now make categorie NOT NULL
ALTER TABLE type_revenu
    ALTER COLUMN categorie SET NOT NULL;

-- Add CHECK constraints
ALTER TABLE type_revenu
    ADD CONSTRAINT chk_type_revenu_categorie 
    CHECK (categorie IN ('PRIME', 'PRIME-PONCTUALITE', 'PRIME-PRESENCE', 'PRIME-REGULARITE', 'FRAIS', 'INDEMNITE', 'COMMISSION', 'RELIQUAT', 'AUTRE'));

ALTER TABLE type_revenu
    ADD CONSTRAINT chk_type_revenu_imposable 
    CHECK (imposable IN ('Y', 'N'));

ALTER TABLE type_revenu
    ADD CONSTRAINT chk_type_revenu_soumis_cotisations 
    CHECK (soumis_cotisations IN ('Y', 'N'));

ALTER TABLE type_revenu
    ADD CONSTRAINT chk_type_revenu_actif 
    CHECK (actif IN ('Y', 'N'));

-- Drop the old unique constraint on code_revenu
ALTER TABLE type_revenu
    DROP CONSTRAINT IF EXISTS type_revenu_code_revenu_key;

-- Create a unique constraint on (entreprise_id, code_revenu)
-- For NULL entreprise_id, we use a partial unique index
CREATE UNIQUE INDEX IF NOT EXISTS idx_type_revenu_code_entreprise_null 
    ON type_revenu (code_revenu) 
    WHERE entreprise_id IS NULL;

-- For non-NULL entreprise_id, we use a unique index
CREATE UNIQUE INDEX IF NOT EXISTS idx_type_revenu_code_entreprise_not_null 
    ON type_revenu (entreprise_id, code_revenu) 
    WHERE entreprise_id IS NOT NULL;

-- Create index on categorie for better query performance
CREATE INDEX IF NOT EXISTS idx_type_revenu_categorie ON type_revenu(categorie);

-- Create index on actif for filtering
CREATE INDEX IF NOT EXISTS idx_type_revenu_actif ON type_revenu(actif);
