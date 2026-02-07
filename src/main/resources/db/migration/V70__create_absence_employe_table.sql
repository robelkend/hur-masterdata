-- Create absence_employe table for managing employee absences/retards
CREATE TABLE IF NOT EXISTS absence_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    employe_id BIGINT NOT NULL,
    emploi_employe_id BIGINT,
    type_evenement VARCHAR(20) NOT NULL CHECK (type_evenement IN ('ABSENCE', 'RETARD')),
    date_jour DATE NOT NULL,
    heure_debut VARCHAR(5),
    heure_fin VARCHAR(5),
    unite_mesure VARCHAR(20) CHECK (unite_mesure IN ('MINUTE', 'HEURE', 'JOUR')),
    quantite NUMERIC(10,2),
    devise_id BIGINT,
    montant_equivalent NUMERIC(18,2),
    payroll_id BIGINT,
    justificatif CHAR(1) NOT NULL DEFAULT 'N' CHECK (justificatif IN ('Y', 'N')),
    motif VARCHAR(80),
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'VALIDE', 'ANNULE')),
    source VARCHAR(20) NOT NULL DEFAULT 'MANUEL' CHECK (source IN ('MANUEL', 'SYSTEME', 'IMPORT')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_absence_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_absence_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE RESTRICT,
    CONSTRAINT fk_absence_employe_emploi FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id) ON DELETE SET NULL,
    CONSTRAINT fk_absence_employe_devise FOREIGN KEY (devise_id) REFERENCES devise(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_absence_employe_employe_id ON absence_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_absence_employe_date_jour ON absence_employe(date_jour);
CREATE INDEX IF NOT EXISTS idx_absence_employe_statut ON absence_employe(statut);
CREATE INDEX IF NOT EXISTS idx_absence_employe_entreprise_id ON absence_employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_absence_employe_source ON absence_employe(source);

COMMENT ON TABLE absence_employe IS 'Table for storing employee absences and late arrivals';
COMMENT ON COLUMN absence_employe.type_evenement IS 'Type of event: ABSENCE or RETARD';
COMMENT ON COLUMN absence_employe.unite_mesure IS 'Unit for normalized quantity: MINUTE, HEURE, JOUR';
COMMENT ON COLUMN absence_employe.montant_equivalent IS 'Equivalent amount calculated from salary or bareme';
COMMENT ON COLUMN absence_employe.payroll_id IS 'Payroll reference (if applicable, not displayed in UI)';
