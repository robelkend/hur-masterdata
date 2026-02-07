-- Create tranche_bareme_deduction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS tranche_bareme_deduction (
    id BIGSERIAL PRIMARY KEY,
    definition_deduction_id BIGINT NOT NULL,
    borne_inf NUMERIC(15,2) NOT NULL,
    borne_sup NUMERIC(15,2),
    type_deduction VARCHAR(20) NOT NULL DEFAULT 'POURCENTAGE' CHECK (type_deduction IN ('PLAT', 'POURCENTAGE')),
    valeur NUMERIC(15,2) NOT NULL,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_tranche_bareme_definition_deduction FOREIGN KEY (definition_deduction_id) REFERENCES definition_deduction(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_borne_order CHECK (borne_sup IS NULL OR borne_sup >= borne_inf)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_tranche_bareme_definition_deduction ON tranche_bareme_deduction(definition_deduction_id);
CREATE INDEX IF NOT EXISTS idx_tranche_bareme_borne_inf ON tranche_bareme_deduction(borne_inf);

-- Add comment to table
COMMENT ON TABLE tranche_bareme_deduction IS 'Table for storing deduction bracket/tranche information. A single tranche can have borne_sup = NULL (last tranche).';
