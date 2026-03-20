CREATE TABLE IF NOT EXISTS payroll_employe_agregat_deduction (
    id BIGSERIAL PRIMARY KEY,
    payroll_employe_agregat_id BIGINT NOT NULL,
    code_deduction VARCHAR(120) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    categorie VARCHAR(20) NOT NULL,
    base_montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    taux NUMERIC(18,4) DEFAULT 0,
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_couvert NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_employe_agregat_deduction_agregat
        FOREIGN KEY (payroll_employe_agregat_id) REFERENCES payroll_employe_agregat(id),
    CONSTRAINT uq_payroll_employe_agregat_deduction
        UNIQUE (payroll_employe_agregat_id, code_deduction, categorie),
    CONSTRAINT ck_payroll_employe_agregat_deduction_categorie
        CHECK (categorie IN ('TAXE', 'COTISATION', 'ASSURANCE', 'SAISIE', 'AUTRE'))
);

CREATE INDEX IF NOT EXISTS idx_pead_agregat
    ON payroll_employe_agregat_deduction(payroll_employe_agregat_id);
