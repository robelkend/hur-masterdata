-- Create rubrique_paie_deduction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS rubrique_paie_deduction (
    id BIGSERIAL PRIMARY KEY,
    definition_deduction_id BIGINT NOT NULL,
    rubrique_paie_id BIGINT NOT NULL,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_rubrique_paie_deduction_definition_deduction FOREIGN KEY (definition_deduction_id) REFERENCES definition_deduction(id) ON DELETE CASCADE,
    CONSTRAINT fk_rubrique_paie_deduction_rubrique_paie FOREIGN KEY (rubrique_paie_id) REFERENCES rubrique_paie(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicates
    CONSTRAINT uk_rubrique_paie_deduction UNIQUE (definition_deduction_id, rubrique_paie_id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_rubrique_paie_deduction_definition ON rubrique_paie_deduction(definition_deduction_id);
CREATE INDEX IF NOT EXISTS idx_rubrique_paie_deduction_rubrique ON rubrique_paie_deduction(rubrique_paie_id);
