CREATE TABLE IF NOT EXISTS payroll_employe_agregat (
    id BIGSERIAL PRIMARY KEY,
    regime_paie_id BIGINT NOT NULL,
    employe_id BIGINT NOT NULL,
    periode_boni_id BIGINT NOT NULL,
    montant_salaire_base NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_supplementaire NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_autre_revenu NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_sanctions NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_payroll_employe_agregat UNIQUE (regime_paie_id, employe_id, periode_boni_id),
    CONSTRAINT fk_payroll_employe_agregat_regime FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id),
    CONSTRAINT fk_payroll_employe_agregat_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_payroll_employe_agregat_periode FOREIGN KEY (periode_boni_id) REFERENCES payroll_periode_boni(id)
);

CREATE INDEX IF NOT EXISTS idx_payroll_employe_agregat_periode ON payroll_employe_agregat(periode_boni_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employe_agregat_employe ON payroll_employe_agregat(employe_id);
