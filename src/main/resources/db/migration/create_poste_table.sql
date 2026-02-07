-- Create poste table
CREATE TABLE IF NOT EXISTS poste (
    id BIGSERIAL PRIMARY KEY,
    code_poste VARCHAR(50) NOT NULL UNIQUE,
    type_salaire VARCHAR(20) NOT NULL CHECK (type_salaire IN ('FIXE', 'HORAIRE', 'JOURNALIER', 'PIECE', 'PIECE_FIXE')),
    description VARCHAR(255) NOT NULL,
    code_devise VARCHAR(50) NOT NULL,
    salaire_min NUMERIC(15, 2) NOT NULL CHECK (salaire_min > 0),
    salaire_max NUMERIC(15, 2) NOT NULL CHECK (salaire_max > 0),
    nb_jour_semaine INTEGER NOT NULL CHECK (nb_jour_semaine >= 1 AND nb_jour_semaine <= 7),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key constraint to devise table
    CONSTRAINT fk_poste_devise 
        FOREIGN KEY (code_devise) 
        REFERENCES devise(code_devise) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Validation constraint: salaire_min <= salaire_max
    CONSTRAINT chk_poste_salaire_range 
        CHECK (salaire_min <= salaire_max)
);

-- Create index on code_poste for faster lookups
CREATE INDEX IF NOT EXISTS idx_poste_code_poste ON poste(code_poste);

-- Create index on code_devise for faster foreign key lookups
CREATE INDEX IF NOT EXISTS idx_poste_code_devise ON poste(code_devise);

-- Add comment to table
COMMENT ON TABLE poste IS 'Table for storing position/job definitions with salary ranges and currency references';
