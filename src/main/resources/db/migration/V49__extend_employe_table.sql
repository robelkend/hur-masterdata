-- Extend employe table with all required fields
ALTER TABLE employe
ADD COLUMN IF NOT EXISTS entreprise_id BIGINT,
ADD COLUMN IF NOT EXISTS matricule_interne VARCHAR(50),
ADD COLUMN IF NOT EXISTS date_naissance DATE,
ADD COLUMN IF NOT EXISTS pays_naissance VARCHAR(2),
ADD COLUMN IF NOT EXISTS pays_habitation VARCHAR(2),
ADD COLUMN IF NOT EXISTS sexe CHAR(1) CHECK (sexe IN ('M', 'F')),
ADD COLUMN IF NOT EXISTS etat_civil VARCHAR(50),
ADD COLUMN IF NOT EXISTS nationalite VARCHAR(2),
ADD COLUMN IF NOT EXISTS langue VARCHAR(2) CHECK (langue IN ('en', 'fr', 'es', 'ht')),
ADD COLUMN IF NOT EXISTS courriel VARCHAR(255),
ADD COLUMN IF NOT EXISTS telephone1 VARCHAR(50),
ADD COLUMN IF NOT EXISTS telephone2 VARCHAR(50),
ADD COLUMN IF NOT EXISTS photo TEXT,
ADD COLUMN IF NOT EXISTS date_embauche DATE,
ADD COLUMN IF NOT EXISTS actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N'));

-- Add foreign key for entreprise
ALTER TABLE employe
ADD CONSTRAINT IF NOT EXISTS fk_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_employe_entreprise ON employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_employe_matricule ON employe(matricule_interne);
CREATE INDEX IF NOT EXISTS idx_employe_actif ON employe(actif);

-- Add comments
COMMENT ON COLUMN employe.entreprise_id IS 'Foreign key to entreprise table (optional)';
COMMENT ON COLUMN employe.code_employe IS 'Unique employee code - generated automatically if param_generation_code_employe exists, otherwise manual entry required';
COMMENT ON COLUMN employe.matricule_interne IS 'Internal employee number';
COMMENT ON COLUMN employe.date_naissance IS 'Date of birth';
COMMENT ON COLUMN employe.pays_naissance IS 'ISO 3166-1 alpha-2 country code for birth country';
COMMENT ON COLUMN employe.pays_habitation IS 'ISO 3166-1 alpha-2 country code for residence country';
COMMENT ON COLUMN employe.sexe IS 'Gender: M (Masculin/Male) or F (Feminin/Female)';
COMMENT ON COLUMN employe.etat_civil IS 'Marital status: MARIE, CELIBATAIRE, DIVORCE, VEUF, CONCUBINAGE, AUTRE';
COMMENT ON COLUMN employe.nationalite IS 'ISO 3166-1 alpha-2 country code for nationality';
COMMENT ON COLUMN employe.langue IS 'Preferred language: en, fr, es, ht';
COMMENT ON COLUMN employe.photo IS 'Employee photo (Base64 or storage URL)';
COMMENT ON COLUMN employe.date_embauche IS 'First hiring date - filled automatically during nomination (not editable on screen)';
COMMENT ON COLUMN employe.actif IS 'Active status: Y or N - activated during nomination, deactivated during termination (not editable on screen)';
