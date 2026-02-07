-- Create autre_revenu_employe table
CREATE TABLE IF NOT EXISTS autre_revenu_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    employe_id BIGINT NOT NULL,
    type_revenu_id BIGINT NOT NULL,
    date_revenu DATE NOT NULL,
    date_effet DATE,
    devise_id BIGINT NOT NULL,
    montant NUMERIC(18,2) NOT NULL,
    commentaire TEXT,
    mode_inclusion VARCHAR(20) NOT NULL DEFAULT 'PROCHAINE_PAIE' CHECK (mode_inclusion IN ('PROCHAINE_PAIE', 'MANUEL')),
    regime_paie_id BIGINT,
    date_inclusion DATE,
    reference VARCHAR(255),
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'REJETE', 'VALIDE', 'ANNULE', 'PAYE')),
    payroll_no INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_autre_revenu_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_autre_revenu_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE RESTRICT,
    CONSTRAINT fk_autre_revenu_employe_type_revenu FOREIGN KEY (type_revenu_id) REFERENCES type_revenu(id) ON DELETE RESTRICT,
    CONSTRAINT fk_autre_revenu_employe_devise FOREIGN KEY (devise_id) REFERENCES devise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_autre_revenu_employe_regime_paie FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id) ON DELETE RESTRICT
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_autre_revenu_employe_entreprise ON autre_revenu_employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_autre_revenu_employe_employe ON autre_revenu_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_autre_revenu_employe_type_revenu ON autre_revenu_employe(type_revenu_id);
CREATE INDEX IF NOT EXISTS idx_autre_revenu_employe_date_revenu ON autre_revenu_employe(date_revenu);
CREATE INDEX IF NOT EXISTS idx_autre_revenu_employe_statut ON autre_revenu_employe(statut);
CREATE INDEX IF NOT EXISTS idx_autre_revenu_employe_payroll_no ON autre_revenu_employe(payroll_no);
