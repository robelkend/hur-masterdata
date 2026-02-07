-- Create definition_deduction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS definition_deduction (
    id BIGSERIAL PRIMARY KEY,
    code_deduction VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    description TEXT,
    type_deduction VARCHAR(20) NOT NULL DEFAULT 'POURCENTAGE' CHECK (type_deduction IN ('PLAT', 'POURCENTAGE')),
    base_limite VARCHAR(20) NOT NULL DEFAULT 'FIXE' CHECK (base_limite IN ('FIXE', 'ANNUEL')),
    entite_id BIGINT,
    arrondir VARCHAR(20) NOT NULL CHECK (arrondir IN ('UNITE', 'DIXIEME', 'CENTIEME', 'MILLIEME')),
    valeur NUMERIC(15,2) NOT NULL DEFAULT 0,
    valeur_couvert NUMERIC(15,2) NOT NULL DEFAULT 0,
    frequence VARCHAR(20) CHECK (frequence IN ('AUCUN', 'JOURNALIER', 'HEBDOMADAIRE', 'QUINZAINE', 'QUIZOMADAIRE', 'MENSUEL')),
    pct_hors_calcul NUMERIC(5,2) DEFAULT 0,
    min_prelevement NUMERIC(15,2) DEFAULT 0,
    max_prelevement NUMERIC(15,2) DEFAULT 0,
    probatoire CHAR(1) NOT NULL DEFAULT 'Y' CHECK (probatoire IN ('Y', 'N')),
    specialise CHAR(1) NOT NULL DEFAULT 'N' CHECK (specialise IN ('Y', 'N')),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_definition_deduction_entite FOREIGN KEY (entite_id) REFERENCES institution_tierse(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_definition_deduction_code ON definition_deduction(code_deduction);
CREATE INDEX IF NOT EXISTS idx_definition_deduction_entite ON definition_deduction(entite_id);
CREATE INDEX IF NOT EXISTS idx_definition_deduction_type_deduction ON definition_deduction(type_deduction);

-- Add comment to table
COMMENT ON TABLE definition_deduction IS 'Table for storing deduction definitions with comprehensive configuration.';
