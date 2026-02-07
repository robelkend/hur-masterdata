-- Drop existing table if it exists (for recreation)
DROP TABLE IF EXISTS supplementaire_employe CASCADE;

-- Recreate supplementaire_employe table with details as JSONB
CREATE TABLE supplementaire_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    employe_id BIGINT NOT NULL,
    memo TEXT NOT NULL,
    date_jour DATE NOT NULL,
    heure_debut VARCHAR(10) NOT NULL,  -- Changed from TIME to VARCHAR for time picker
    heure_fin VARCHAR(10) NOT NULL,    -- Changed from TIME to VARCHAR for time picker
    type_supplementaire VARCHAR(30) NOT NULL CHECK (type_supplementaire IN ('HEURE', 'FERIE', 'NUIT', 'WEEKEND', 'OFF', 'CONGE', 'AUTRE')),
    base_calcul VARCHAR(30) CHECK (base_calcul IN ('SALAIRE_BASE', 'TAUX_HORAIRE', 'FIXE')),
    montant_base NUMERIC(18,2),
    devise_id BIGINT,
    montant_calcule NUMERIC(18,2),
    no_presence INTEGER NOT NULL DEFAULT 0,
    no_payroll INTEGER NOT NULL DEFAULT 0,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,  -- Contains nb_heures, nb_jours, nb_nuits, nb_offs, nb_conges, montant_*_calcule
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'VALIDE')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_supplementaire_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_supplementaire_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE RESTRICT,
    CONSTRAINT fk_supplementaire_employe_devise FOREIGN KEY (devise_id) REFERENCES devise(id) ON DELETE RESTRICT
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_supplementaire_employe_employe_id ON supplementaire_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_supplementaire_employe_date_jour ON supplementaire_employe(date_jour);
CREATE INDEX IF NOT EXISTS idx_supplementaire_employe_statut ON supplementaire_employe(statut);
CREATE INDEX IF NOT EXISTS idx_supplementaire_employe_entreprise_id ON supplementaire_employe(entreprise_id);

-- Add comments
COMMENT ON TABLE supplementaire_employe IS 'Table for storing employee supplementary work entries (overtime, holiday work, night work, etc.)';
COMMENT ON COLUMN supplementaire_employe.memo IS 'Memo/description for the supplementary work entry (required)';
COMMENT ON COLUMN supplementaire_employe.date_jour IS 'Date of the supplementary work';
COMMENT ON COLUMN supplementaire_employe.heure_debut IS 'Start time of the supplementary work (stored as VARCHAR, displayed as time picker)';
COMMENT ON COLUMN supplementaire_employe.heure_fin IS 'End time of the supplementary work (stored as VARCHAR, displayed as time picker)';
COMMENT ON COLUMN supplementaire_employe.type_supplementaire IS 'Type of supplementary work: HEURE, FERIE, NUIT, WEEKEND, OFF, CONGE, AUTRE';
COMMENT ON COLUMN supplementaire_employe.base_calcul IS 'Calculation base: SALAIRE_BASE, TAUX_HORAIRE, FIXE';
COMMENT ON COLUMN supplementaire_employe.statut IS 'Status: BROUILLON (draft) or VALIDE (validated)';
COMMENT ON COLUMN supplementaire_employe.details IS 'JSONB containing: nb_heures, nb_jours, nb_nuits, nb_offs, nb_conges, montant_heure_calcule, montant_jour_calcule, montant_nuit_calcule, montant_off_calcule, montant_conge_calcule';
COMMENT ON COLUMN supplementaire_employe.no_presence IS 'Presence number reference (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.no_payroll IS 'Payroll number reference (internal, not displayed in UI)';
