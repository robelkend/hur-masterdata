-- Create supplementaire_employe table for managing employee supplementary work
CREATE TABLE IF NOT EXISTS supplementaire_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    employe_id BIGINT NOT NULL,
    memo TEXT NOT NULL,
    date_jour DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    type_supplementaire VARCHAR(30) NOT NULL CHECK (type_supplementaire IN ('HEURE', 'FERIE', 'NUIT', 'WEEKEND', 'OFF', 'CONGE', 'AUTRE')),
    nb_heures NUMERIC(10,2) NOT NULL DEFAULT 0,
    nb_jours NUMERIC(10,2) NOT NULL DEFAULT 0,
    nb_nuits NUMERIC(10,2) NOT NULL DEFAULT 0,
    nb_offs NUMERIC(10,2) NOT NULL DEFAULT 0,
    nb_conges NUMERIC(10,2) NOT NULL DEFAULT 0,
    base_calcul VARCHAR(30) CHECK (base_calcul IN ('SALAIRE_BASE', 'TAUX_HORAIRE', 'FIXE')),
    montant_base NUMERIC(18,2),
    devise_id BIGINT,
    montant_calcule NUMERIC(18,2),
    montant_heure_calcule NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_jour_calcule NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_nuit_calcule NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_off_calcule NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_conge_calcule NUMERIC(18,2) NOT NULL DEFAULT 0,
    no_presence INTEGER NOT NULL DEFAULT 0,
    no_payroll INTEGER NOT NULL DEFAULT 0,
    statut VARCHAR(20) NOT NULL DEFAULT 'NOUVEAU' CHECK (statut IN ('BROUILLON', 'VALIDE')),
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
COMMENT ON COLUMN supplementaire_employe.heure_debut IS 'Start time of the supplementary work';
COMMENT ON COLUMN supplementaire_employe.heure_fin IS 'End time of the supplementary work';
COMMENT ON COLUMN supplementaire_employe.type_supplementaire IS 'Type of supplementary work: HEURE, FERIE, NUIT, WEEKEND, OFF, CONGE, AUTRE';
COMMENT ON COLUMN supplementaire_employe.base_calcul IS 'Calculation base: SALAIRE_BASE, TAUX_HORAIRE, FIXE';
COMMENT ON COLUMN supplementaire_employe.statut IS 'Status: NOUVEAU (new) or VALIDE (validated)';
COMMENT ON COLUMN supplementaire_employe.montant_heure_calcule IS 'Calculated amount for hours (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.montant_jour_calcule IS 'Calculated amount for days (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.montant_nuit_calcule IS 'Calculated amount for nights (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.montant_off_calcule IS 'Calculated amount for off-days (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.montant_conge_calcule IS 'Calculated amount for leave days (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.no_presence IS 'Presence number reference (internal, not displayed in UI)';
COMMENT ON COLUMN supplementaire_employe.no_payroll IS 'Payroll number reference (internal, not displayed in UI)';
