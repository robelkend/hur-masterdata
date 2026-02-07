-- Create regime_paie_deduction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS regime_paie_deduction (
    id BIGSERIAL PRIMARY KEY,
    regime_paie_id BIGINT NOT NULL,
    deduction_code_id BIGINT NOT NULL,
    exclusif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (exclusif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_regime_paie_deduction_regime_paie FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id),
    CONSTRAINT fk_regime_paie_deduction_definition_deduction FOREIGN KEY (deduction_code_id) REFERENCES definition_deduction(id),
    CONSTRAINT uk_regime_paie_deduction UNIQUE (regime_paie_id, deduction_code_id)
);

CREATE INDEX IF NOT EXISTS idx_regime_paie_deduction_regime_paie ON regime_paie_deduction(regime_paie_id);
CREATE INDEX IF NOT EXISTS idx_regime_paie_deduction_definition_deduction ON regime_paie_deduction(deduction_code_id);
