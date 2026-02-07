-- Create exclusion_deduction table for excluding deductions by type employe
CREATE TABLE IF NOT EXISTS exclusion_deduction (
    id BIGSERIAL PRIMARY KEY,
    type_employe_id BIGINT NOT NULL,
    definition_deduction_id BIGINT NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_exclusion_deduction_type_employe FOREIGN KEY (type_employe_id) REFERENCES type_employe(id) ON DELETE RESTRICT,
    CONSTRAINT fk_exclusion_deduction_definition_deduction FOREIGN KEY (definition_deduction_id) REFERENCES definition_deduction(id) ON DELETE RESTRICT,
    CONSTRAINT uq_exclusion_deduction UNIQUE (type_employe_id, definition_deduction_id)
);

CREATE INDEX IF NOT EXISTS idx_exclusion_deduction_type_employe ON exclusion_deduction(type_employe_id);
CREATE INDEX IF NOT EXISTS idx_exclusion_deduction_definition ON exclusion_deduction(definition_deduction_id);
