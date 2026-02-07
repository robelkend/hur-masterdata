-- Create pret_employe table
CREATE TABLE IF NOT EXISTS pret_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    employe_id BIGINT NOT NULL,
    date_pret DATE NOT NULL,
    devise_id BIGINT NOT NULL,
    montant_pret NUMERIC(15,2) NOT NULL DEFAULT 0,
    montant_subvention NUMERIC(15,2) NOT NULL DEFAULT 0,
    periodicite VARCHAR(50) NOT NULL CHECK (periodicite IN ('JOURNALIER', 'HEBDO', 'QUINZAINE', 'QUINZOMADAIRE', 'TRIMESTRIEL', 'SEMESTRIEL', 'ANNUEL')),
    prelever_dans_payroll CHAR(1) DEFAULT 'Y' NOT NULL CHECK (prelever_dans_payroll IN ('Y', 'N')),
    prelevement_partiel CHAR(1) DEFAULT 'N' NOT NULL CHECK (prelevement_partiel IN ('Y', 'N')),
    nb_prevu INTEGER NOT NULL DEFAULT 1,
    montant_periode NUMERIC(15,2) NOT NULL DEFAULT 0,
    montant_verse NUMERIC(15,2) NOT NULL DEFAULT 0,
    premier_prelevement DATE,
    dernier_prelevement DATE,
    type_interet VARCHAR(50) CHECK (type_interet IN ('PLAT', 'POURCENTAGE')),
    taux_interet NUMERIC(10,4) DEFAULT 0,
    avance CHAR(1) DEFAULT 'N' NOT NULL CHECK (avance IN ('Y', 'N')),
    libelle VARCHAR(255),
    note TEXT NOT NULL,
    ordre INTEGER NOT NULL DEFAULT 1,
    regime_paie_id BIGINT,
    type_revenu_id BIGINT,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'EN_COURS', 'TERMINE', 'ANNULE', 'SUSPENDU')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_pret_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pret_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pret_employe_devise FOREIGN KEY (devise_id) REFERENCES devise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pret_employe_regime_paie FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pret_employe_type_revenu FOREIGN KEY (type_revenu_id) REFERENCES type_revenu(id) ON DELETE RESTRICT
);

-- Create pret_remboursement table
CREATE TABLE IF NOT EXISTS pret_remboursement (
    id BIGSERIAL PRIMARY KEY,
    pret_employe_id BIGINT NOT NULL,
    date_remboursement DATE NOT NULL,
    montant_rembourse NUMERIC(15,2) NOT NULL,
    montant_interet NUMERIC(15,2) NOT NULL,
    montant_total NUMERIC(15,2) NOT NULL,
    origine VARCHAR(50) NOT NULL CHECK (origine IN ('PAIE', 'MANUEL', 'AJUSTEMENT')),
    no_payroll INTEGER NOT NULL DEFAULT 0,
    commentaire TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_pret_remboursement_pret FOREIGN KEY (pret_employe_id) REFERENCES pret_employe(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_pret_employe_employe_id ON pret_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_pret_employe_statut ON pret_employe(statut);
CREATE INDEX IF NOT EXISTS idx_pret_employe_date_pret ON pret_employe(date_pret);
CREATE INDEX IF NOT EXISTS idx_pret_employe_entreprise_id ON pret_employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_pret_remboursement_pret_employe_id ON pret_remboursement(pret_employe_id);
CREATE INDEX IF NOT EXISTS idx_pret_remboursement_date_remboursement ON pret_remboursement(date_remboursement);
CREATE INDEX IF NOT EXISTS idx_pret_remboursement_no_payroll ON pret_remboursement(no_payroll);

-- Add comments
COMMENT ON TABLE pret_employe IS 'Table for storing employee loans/advances';
COMMENT ON TABLE pret_remboursement IS 'Table for storing loan repayment records';
COMMENT ON COLUMN pret_employe.montant_verse IS 'Calculated automatically from pret_remboursement, cannot be modified directly';
COMMENT ON COLUMN pret_employe.dernier_prelevement IS 'Calculated automatically, cannot be modified directly';
COMMENT ON COLUMN pret_employe.statut IS 'Status: BROUILLON (draft), EN_COURS (active), TERMINE (completed), ANNULE (cancelled), SUSPENDU (suspended)';
