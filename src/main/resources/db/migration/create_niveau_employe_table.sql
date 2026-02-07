-- Create niveau_employe table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS niveau_employe (
    id BIGSERIAL PRIMARY KEY,
    code_niveau VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    niveau_hierarchique INTEGER NOT NULL CHECK (niveau_hierarchique >= 0),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_niveau_employe_code_niveau ON niveau_employe(code_niveau);
CREATE INDEX IF NOT EXISTS idx_niveau_employe_description ON niveau_employe(description);
CREATE INDEX IF NOT EXISTS idx_niveau_employe_niveau_hierarchique ON niveau_employe(niveau_hierarchique);

-- Add comment to table
COMMENT ON TABLE niveau_employe IS 'Table for storing employee levels with hierarchical structure.';
