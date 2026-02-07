-- Create plan_assurance table
CREATE TABLE IF NOT EXISTS plan_assurance (
    id BIGSERIAL PRIMARY KEY,
    code_plan VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255),
    description VARCHAR(255) NOT NULL,
    code_payroll VARCHAR(50),
    type_prelevement VARCHAR(20) NOT NULL CHECK (type_prelevement IN ('PLAT', 'POURCENTAGE')),
    valeur NUMERIC(15, 2) NOT NULL CHECK (valeur > 0),
    valeur_couverte NUMERIC(15, 2) NOT NULL CHECK (valeur_couverte > 0),
    code_institution VARCHAR(50) NOT NULL,
    categorie VARCHAR(20) NOT NULL CHECK (categorie IN ('MEDICAL', 'EMPLOI', 'VIEILLESSE', 'AUTRE')),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key constraint to reference_payroll table (nullable)
    CONSTRAINT fk_plan_assurance_reference_payroll 
        FOREIGN KEY (code_payroll) 
        REFERENCES reference_payroll(code_payroll) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    
    -- Foreign key constraint to institution_tierse table (required)
    CONSTRAINT fk_plan_assurance_institution_tierse 
        FOREIGN KEY (code_institution) 
        REFERENCES institution_tierse(code_institution) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
);

-- Create index on code_plan for faster lookups
CREATE INDEX IF NOT EXISTS idx_plan_assurance_code_plan ON plan_assurance(code_plan);

-- Create index on code_payroll for faster foreign key lookups
CREATE INDEX IF NOT EXISTS idx_plan_assurance_code_payroll ON plan_assurance(code_payroll);

-- Create index on code_institution for faster foreign key lookups
CREATE INDEX IF NOT EXISTS idx_plan_assurance_code_institution ON plan_assurance(code_institution);

-- Add comment to table
COMMENT ON TABLE plan_assurance IS 'Table for storing insurance plan definitions with payroll and institution references';
