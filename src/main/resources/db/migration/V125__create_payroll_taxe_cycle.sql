CREATE TABLE IF NOT EXISTS payroll_taxe_cycle (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    regime_paie_id BIGINT NOT NULL,
    dernier_payroll_taxe_id BIGINT,
    dernier_taxe_date_fin DATE,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_payroll_taxe_cycle_employe_regime UNIQUE (employe_id, regime_paie_id),
    CONSTRAINT fk_payroll_taxe_cycle_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_payroll_taxe_cycle_regime FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id),
    CONSTRAINT fk_payroll_taxe_cycle_payroll FOREIGN KEY (dernier_payroll_taxe_id) REFERENCES payroll(id)
);

CREATE INDEX IF NOT EXISTS idx_payroll_taxe_cycle_employe ON payroll_taxe_cycle(employe_id);
CREATE INDEX IF NOT EXISTS idx_payroll_taxe_cycle_regime ON payroll_taxe_cycle(regime_paie_id);
