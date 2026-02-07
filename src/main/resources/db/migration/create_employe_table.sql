-- Create employe table (ébauche - skeleton)
-- This is a basic structure that will be extended later
CREATE TABLE IF NOT EXISTS employe (
    id BIGSERIAL PRIMARY KEY,
    code_employe VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create index on code_employe for faster lookups
CREATE INDEX IF NOT EXISTS idx_employe_code_employe ON employe(code_employe);

-- Create index on nom for faster searches
CREATE INDEX IF NOT EXISTS idx_employe_nom ON employe(nom);

-- Add comment to table
COMMENT ON TABLE employe IS 'Table for storing employee basic information (ébauche - will be extended later)';
COMMENT ON COLUMN employe.code_employe IS 'Unique employee code';
COMMENT ON COLUMN employe.nom IS 'Employee last name';
COMMENT ON COLUMN employe.prenom IS 'Employee first name';
