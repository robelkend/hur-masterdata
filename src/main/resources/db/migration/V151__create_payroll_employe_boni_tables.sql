CREATE TABLE IF NOT EXISTS payroll_employe_boni (
    id BIGSERIAL PRIMARY KEY,
    statut VARCHAR(30) NOT NULL DEFAULT 'CALCULE',
    rubrique_paie_id BIGINT NOT NULL,
    regime_paie_id BIGINT NOT NULL,
    periode_boni_id BIGINT NOT NULL,
    employe_id BIGINT NOT NULL,
    montant_reference NUMERIC(18,2) NOT NULL DEFAULT 0,
    diviseur NUMERIC(18,4) NOT NULL DEFAULT 1,
    montant_boni_brut NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_deductions NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_boni_net NUMERIC(18,2) NOT NULL DEFAULT 0,
    formule VARCHAR(255) NOT NULL,
    email_envoye VARCHAR(1) NOT NULL DEFAULT 'N',
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT ck_payroll_employe_boni_statut CHECK (statut IN ('CALCULE', 'VALIDE')),
    CONSTRAINT ck_payroll_employe_boni_email CHECK (email_envoye IN ('Y', 'N')),
    CONSTRAINT fk_payroll_employe_boni_rubrique FOREIGN KEY (rubrique_paie_id) REFERENCES rubrique_paie(id),
    CONSTRAINT fk_payroll_employe_boni_regime FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id),
    CONSTRAINT fk_payroll_employe_boni_periode FOREIGN KEY (periode_boni_id) REFERENCES periode_paie(id),
    CONSTRAINT fk_payroll_employe_boni_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT uq_payroll_employe_boni_unique UNIQUE (rubrique_paie_id, regime_paie_id, periode_boni_id, employe_id)
);

CREATE INDEX IF NOT EXISTS idx_payroll_employe_boni_periode ON payroll_employe_boni(periode_boni_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employe_boni_regime ON payroll_employe_boni(regime_paie_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employe_boni_employe ON payroll_employe_boni(employe_id);

CREATE TABLE IF NOT EXISTS payroll_boni_deduction (
    id BIGSERIAL PRIMARY KEY,
    payroll_boni_id BIGINT NOT NULL,
    employe_id BIGINT NOT NULL,
    code_deduction VARCHAR(120) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    base_montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    taux NUMERIC(18,4) NOT NULL DEFAULT 0,
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_couvert NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_boni_deduction_boni FOREIGN KEY (payroll_boni_id) REFERENCES payroll_employe_boni(id),
    CONSTRAINT fk_payroll_boni_deduction_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT uq_payroll_boni_deduction_unique UNIQUE (payroll_boni_id, code_deduction)
);

CREATE INDEX IF NOT EXISTS idx_payroll_boni_deduction_boni ON payroll_boni_deduction(payroll_boni_id);
