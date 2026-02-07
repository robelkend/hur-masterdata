-- Create payroll_recouvrement table
CREATE TABLE IF NOT EXISTS payroll_recouvrement (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    payroll_employe_id BIGINT NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    type_recouvrement VARCHAR(20) NOT NULL DEFAULT 'PRET' CHECK (type_recouvrement IN ('PRET', 'AVANCE', 'AUTRE')),
    reference_no VARCHAR(120),
    montant_periode NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_interet NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_total NUMERIC(18,2) NOT NULL DEFAULT 0,
    solde_avant NUMERIC(18,2),
    solde_apres NUMERIC(18,2),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_recouvrement_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_recouvrement_payroll_employe FOREIGN KEY (payroll_employe_id) REFERENCES payroll_employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_recouvrement_payroll ON payroll_recouvrement(payroll_id);
